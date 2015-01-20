package ljfa.noitemrot;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.common.FMLLog;

public class RenderItemTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(name.equals("net.minecraft.client.renderer.entity.RenderItem")) {
            FMLLog.log("NoItemRotation", Level.INFO, "About to patch class %s", name);
            return patchClassASM(name, basicClass);
        } else if(name.equals("bny")) {
            FMLLog.log("NoItemRotation", Level.INFO, "About to patch obfuscated class %s", name);
            return patchClassASM(name, basicClass);
        } else
            return basicClass;
    }

    public byte[] patchClassASM(String name, byte[] bytes) {      
        //ASM manipulation stuff
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        //Loop through the methods until we find our target
        for(MethodNode mn: classNode.methods) {
            if((mn.name.equals("doRender") || mn.name.equals("func_76986_a")) && mn.desc.equals("(Lnet/minecraft/entity/item/EntityItem;DDDFF)V")) {
                FMLLog.log("NoItemRotation", Level.INFO, "Found target method %s%s", mn.name, mn.desc);
                patchDoRender(mn);
            }
            else if(mn.name.equals("renderDroppedItem") && mn.desc.equals("(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/util/IIcon;IFFFFI)V")) {
                FMLLog.log("NoItemRotation", Level.INFO, "Found target method %s%s", mn.name, mn.desc);
                patchRenderDroppedItem(mn);
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
        while(it.hasNext()) {
            AbstractInsnNode currentNode = it.next();
            /* In the RenderItem class, at line 70:
             * 
             * Currently, the item's rotation angle is computed and stored in field f3.
             * The 12 instructions before that are where said angle is computed.
             * We want to remove this computation and instead just store zero as angle into f3.
             * 
             * The way we do this is look for the instruction "fstore 12".
             * This is the access to the field f3.
             * We simply discard the 12 preceding instructions as we don't want to compute any angle.
             * Instead, we just load the constant 0.0f and store that into f3.
             */
            //Search for "fstore 12"
            if(currentNode.getOpcode() == Opcodes.FSTORE) {
                VarInsnNode node = (VarInsnNode)currentNode;
                if(node.var == 12) {
                    //Found "fstore 12"
                    FMLLog.log("NoItemRotation", Level.INFO, "Found target instruction \"fstore 12\"");

                    //Remove the 12 preceding instructions
                    for(int i = 0; i < 12; i++)
                        mn.instructions.remove(node.getPrevious());

                    //Insert a "fconst_0" instruction
                    mn.instructions.insertBefore(node, new InsnNode(Opcodes.FCONST_0));

                    FMLLog.log("NoItemRotation", Level.INFO, "Successfully injected into %s%s", mn.name, mn.desc);
                    break;
                }
            }
        }
    }
    
    private void patchRenderDroppedItem(MethodNode mn) {
        //Loop through the instructions of the method
        Iterator<AbstractInsnNode> it = mn.instructions.iterator();
        while(it.hasNext()) {
            AbstractInsnNode currentNode = it.next();
            /* In the RenderItem class, at line 286:
             * 
             * Notice how similar this is to the angle computation in doRender.
             * However, this time the value is not stored in a field but instead passed to a method.
             * It's slightly harder to identify the section we want to modify here.
             * We can look for the first "fdiv" instruction in this method. Notice how before that point
             * no floating point divisions are being made in this function.
             * 
             * This time we want to skip the whole method call, so no rotation is being performed at this point.
             * The computation and the method call include the 6 instructions before "fdiv",
             * "fdiv" itself and the 9 instructions after this. We want to remove all this.
             * 
             * Notice how right before this section is a "goto" instruction. The "goto" points right after
             * this section, so if we remove the section the "goto" will become useless.
             * So we can remove this "goto" as well.
             */
            //Searching for "fdiv" instruction
            if(currentNode.getOpcode() == Opcodes.FDIV) {
                //Found "fdiv"
                InsnNode node = (InsnNode)currentNode;
                FMLLog.log("NoItemRotation", Level.INFO, "Found target instruction \"fdiv\"");
                //Remove the 7 preceding instructions
                for(int i = 0; i < 7; i++)
                    mn.instructions.remove(node.getPrevious());
                //Remove the 9 following instructions
                for(int i = 0; i < 9; i++)
                    mn.instructions.remove(node.getNext());

                //Remove the instruction itself
                mn.instructions.remove(node);

                FMLLog.log("NoItemRotation", Level.INFO, "Successfully injected into %s%s", mn.name, mn.desc);
                break;
            }
        }
    }
}
