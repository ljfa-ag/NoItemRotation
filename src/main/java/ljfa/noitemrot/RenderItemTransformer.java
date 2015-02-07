package ljfa.noitemrot;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.common.FMLLog;

public class RenderItemTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(name.equals("net.minecraft.client.renderer.entity.RenderItem")) {
            FMLLog.log("NoItemRotation", Level.INFO, "About to patch class %s", name);
            return patchClassASM(name, basicClass, false);
        } else if(name.equals("bny")) {
            FMLLog.log("NoItemRotation", Level.INFO, "About to patch obfuscated class %s", name);
            return patchClassASM(name, basicClass, true);
        } else
            return basicClass;
    }

    private byte[] patchClassASM(String name, byte[] bytes, boolean obfuscated) {      
        //ASM manipulation stuff
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        //Loop through the methods until we find our target
        for(MethodNode mn: classNode.methods) {
            if(mn.name.equals(obfuscated ? "func_76986_a" : "doRender") && mn.desc.equals("(Lnet/minecraft/entity/item/EntityItem;DDDFF)V")) {
                FMLLog.log("NoItemRotation", Level.INFO, "Found target method %s%s", mn.name, mn.desc);
                patchDoRender(mn);
            }
            else if(mn.name.equals("renderDroppedItem") && mn.desc.equals("(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/util/IIcon;IFFFFI)V")) {
                FMLLog.log("NoItemRotation", Level.INFO, "Found target method %s%s", mn.name, mn.desc);
                patchRenderDroppedItem(mn);
            }
            else if(Config.disableBobbing && mn.name.equals("shouldBob") && mn.desc.equals("()Z")) {
                FMLLog.log("NoItemRotation", Level.INFO, "Found target method %s%s", mn.name, mn.desc);
                patchShouldBob(mn);
            }
        }

        //Write class
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private void patchDoRender(MethodNode mn) {
        //Loop through the instructions of the method
        Iterator<AbstractInsnNode> it = mn.instructions.iterator();
        boolean didInject = false;
        while(it.hasNext()) {
            AbstractInsnNode currentNode = it.next();
            /* In the RenderItem class, at line 70:
             * 
             * Currently, the item's rotation angle is computed and stored in field f3.
             * The 12 instructions before that are where said angle is computed.
             * We want to skip this computation and instead just store zero as angle into f3.
             * 
             * The way we do this is look for the instruction "fstore 12".
             * This is the access to the field f3. 
             * Then we go 12 steps back and insert a "goto" instruction there, skipping the entire
             * computation.
             * At the end of the skip we insert a "fconst_0" instruction.
             */
            //Search for "fstore 12"
            if(currentNode.getOpcode() == Opcodes.FSTORE) {
                //Check if the argument is 12 and the preceding instruction is "fmul"
                if(((VarInsnNode)currentNode).var == 12 && currentNode.getPrevious().getOpcode() == Opcodes.FMUL) {
                    FMLLog.log("NoItemRotation", Level.INFO, "Found target instruction \"fstore 12\" preceded by \"fmul\"");

                    //Go 12 steps back
                    AbstractInsnNode skipNode = currentNode;
                    for(int i = 0; i < 12; i++)
                        skipNode = skipNode.getPrevious();
                    
                    //Check if it's "aload_1"
                    if(skipNode.getOpcode() == Opcodes.ALOAD && ((VarInsnNode)skipNode).var == 1) {
                        FMLLog.log("NoItemRotation", Level.INFO, "Found target instruction \"aload 1\"");
                        
                        //Insert an "fconst_0" instruction at the end of the skip
                        mn.instructions.insertBefore(currentNode, new InsnNode(Opcodes.FCONST_0));
                        //Insert a label before that
                        currentNode = currentNode.getPrevious();
                        LabelNode label = new LabelNode();
                        mn.instructions.insertBefore(currentNode, label);
                        
                        //Insert a "goto" instruction at the start of the skip
                        mn.instructions.insertBefore(skipNode, new JumpInsnNode(Opcodes.GOTO, label));
                        
                        didInject = true;
                        break;
                    }
                }
            }
        }
        if(didInject)
            FMLLog.log("NoItemRotation", Level.INFO, "Successfully injected into %s%s", mn.name, mn.desc);
        else
            FMLLog.log("NoItemRotation", Level.ERROR, "Failed injection into %s%s", mn.name, mn.desc);
    }
    
    private void patchRenderDroppedItem(MethodNode mn) {
        //Loop through the instructions of the method
        Iterator<AbstractInsnNode> it = mn.instructions.iterator();
        boolean didInject = false;
        while(it.hasNext()) {
            AbstractInsnNode currentNode = it.next();
            /* In the RenderItem class, at line 280:
             * 
             * We want to disable the "else" branch of this branching.
             * 
             * We search for an "ifeq" instruction preceded by "getstatic".
             * 8 steps (including frame and line number nodes) after that
             * should be a "goto" instruction. This instruction jumps after the end of
             * the whole branching. We want to get the target of this "goto" and put it
             * into the "ifeq" instruction, so that when the "if"-test fails, it doesn't go
             * to the "else"-branch but instead right to the end.
             */
            //Searching for "ifeq" instruction, preceded by "getstatic"
            if(currentNode.getOpcode() == Opcodes.IFEQ && currentNode.getPrevious().getOpcode() == Opcodes.GETSTATIC) {
                JumpInsnNode ifeqNode = (JumpInsnNode)currentNode;
                FMLLog.log("NoItemRotation", Level.INFO, "Found target instruction \"ifeq\" preceded by \"getstatic\"");
                
                //Go 8 steps forward
                AbstractInsnNode gotoNode = ifeqNode;
                for(int i = 0; i < 8; i++)
                    gotoNode = gotoNode.getNext();
                
                //Check if it's "goto"
                if(gotoNode.getOpcode() == Opcodes.GOTO) {
                    FMLLog.log("NoItemRotation", Level.INFO, "Found target instruction \"goto\"");
                    //Put the "goto" target into the "ifeq" instruction
                    ifeqNode.label = ((JumpInsnNode)gotoNode).label;
                    
                    didInject = true;
                    break;
                }
            }
        }
        if(didInject)
            FMLLog.log("NoItemRotation", Level.INFO, "Successfully injected into %s%s", mn.name, mn.desc);
        else
            FMLLog.log("NoItemRotation", Level.ERROR, "Failed injection into %s%s", mn.name, mn.desc);
    }
    
    private void patchShouldBob(MethodNode mn) {
        //Loop through the instructions of the method
        Iterator<AbstractInsnNode> it = mn.instructions.iterator();
        boolean didInject = false;
        while(it.hasNext()) {
            AbstractInsnNode currentNode = it.next();
            /* In the RenderItem class, line 803:
             * 
             * We just need to change "iconst_1" to "iconst_0".
             */
            if(currentNode.getOpcode() == Opcodes.ICONST_1) {
                FMLLog.log("NoItemRotation", Level.INFO, "Found target instruction \"iconst_1\"");
                //Replace with "iconst_0"
                mn.instructions.set(currentNode, new InsnNode(Opcodes.ICONST_0));
                didInject = true;
                break;
            }
        }
        if(didInject)
            FMLLog.log("NoItemRotation", Level.INFO, "Successfully injected into %s%s", mn.name, mn.desc);
        else
            FMLLog.log("NoItemRotation", Level.ERROR, "Failed injection into %s%s", mn.name, mn.desc);
    }
}
