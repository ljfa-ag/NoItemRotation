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
            return patchClassASM(name, basicClass, true);
        } else
            return basicClass;
    }

    public byte[] patchClassASM(String name, byte[] bytes, boolean obfuscated) {      
        //ASM manipulation stuff
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        
        //Loop through the methods until we find our target
        for(MethodNode mn: classNode.methods) {
            if(mn.name.equals("doRender") && mn.desc.equals("(Lnet/minecraft/entity/item/EntityItem;DDDFF)V")) {
                FMLLog.log("NoItemRotation", Level.INFO, "Found target method %s", mn.name);
                
                //Loop through the instructions of the method
                Iterator<AbstractInsnNode> it = mn.instructions.iterator();
                while(it.hasNext()) {
                    AbstractInsnNode currentNode = it.next();
                    //We're searching for a "fstore 12" instruction
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
                            
                            FMLLog.log("NoItemRotation", Level.INFO, "Successfully injected into %s", mn.name);
                            break;
                        }
                    }
                }
            } else if(mn.name.equals("renderDroppedItem") && mn.desc.equals("(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/util/IIcon;IFFFFI)V")) {
                FMLLog.log("NoItemRotation", Level.INFO, "Found target method %s", mn.name);
                
                //Loop through the instructions of the method
                Iterator<AbstractInsnNode> it = mn.instructions.iterator();
                while(it.hasNext()) {
                    AbstractInsnNode currentNode = it.next();
                    //We're searching for a "getfield 22" (field name: age) instruction
                    if(currentNode.getOpcode() == Opcodes.GETFIELD) {
                        FieldInsnNode node = (FieldInsnNode)currentNode;
                        if(node.name.equals("age")) {
                            //Found "getfield 22"
                            FMLLog.log("NoItemRotation", Level.INFO, "Found target instruction \"getfield 22\"");
                            //Remove thr two preceding instructions
                            mn.instructions.remove(node.getPrevious());
                            mn.instructions.remove(node.getPrevious());
                            //Remove the 14 following instructions
                            for(int i = 0; i < 14; i++)
                                mn.instructions.remove(node.getNext());
                            
                            //Remove the instruction itself
                            mn.instructions.remove(node);
                            
                            FMLLog.log("NoItemRotation", Level.INFO, "Successfully injected into %s", mn.name);
                            break;
                        }
                    }
                }
            }
        }
        
        //Write class
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
