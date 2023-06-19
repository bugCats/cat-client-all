package cc.bugcat.catserver.asm;

import org.springframework.asm.Type;

import java.util.ArrayList;
import java.util.List;

class CatTypeTools {


    /**
     * @param methodDescriptor (I[DZLjava/util/List;Ljava/lang/Void;J)V
     * @return Ljava/lang/Void;
     * */
    public static String getReturnType(final String methodDescriptor){
        int returnTypeOf = methodDescriptor.indexOf(")") + 1;
        CatTypeDesc desc = getType(methodDescriptor.substring(returnTypeOf).toCharArray(), 0);
        return desc.full;
    }
    
    
    /**
     * @param methodDescriptor (I[DZLjava/util/List;Ljava/lang/Void;J)Lcc/bugcat/example/tools/ResponseEntity;
     * @return ["I", "[D", "Z", "Ljava/util/List;", "Ljava/lang/Void;", "J"]
     * */
    public static List<String> getArgumentTypes(final String methodDescriptor) {
        char[] descChars = methodDescriptor.toCharArray();
        List<String> args = new ArrayList<>();
        int offset = 1;
        while ( descChars[offset] != ')') {
            CatTypeDesc typeDesc = getType(descChars, offset);
            args.add(typeDesc.descriptor);
            offset += typeDesc.length + typeDesc.off;
        }
        return args;
    }


    private static CatTypeDesc getType(final char[] buf, final int off) {
        int len = 0;
        switch (buf[off]) {
            case 'V':
                return new CatTypeDesc(Type.VOID_TYPE, Type.getType(Void.class));
            case 'Z':
                return new CatTypeDesc(Type.BOOLEAN_TYPE, Type.getType(Boolean.class));
            case 'C':
                return new CatTypeDesc(Type.CHAR_TYPE, Type.getType(Character.class));
            case 'B':
                return new CatTypeDesc(Type.BYTE_TYPE, Type.getType(Byte.class));
            case 'S':
                return new CatTypeDesc(Type.SHORT_TYPE, Type.getType(Short.class));
            case 'I':
                return new CatTypeDesc(Type.INT_TYPE, Type.getType(Integer.class));
            case 'F':
                return new CatTypeDesc(Type.FLOAT_TYPE, Type.getType(Float.class));
            case 'J':
                return new CatTypeDesc(Type.LONG_TYPE, Type.getType(Long.class));
            case 'D':
                return new CatTypeDesc(Type.DOUBLE_TYPE, Type.getType(Double.class));
            case '[':
                len = 1;
                while (buf[off + len] == '[') {
                    len ++;
                }
                if (buf[off + len] == 'L') {
                    int stack = 0;
                    while ( len < buf.length ) {
                        char cr = buf[off + len];
                        if( cr == ';' ){
                            if( stack == 0 ){
                                break;
                            }
                        } else if( cr == '<' ){
                            stack ++ ;
                        } else if( cr == '>' ){
                            stack --;
                        }
                        len ++;
                    }
                }
                return new CatTypeDesc(false, new String(buf, off, len + 1));
            case 'L':
                len = 1;
                int stack = 0;
                while ( len < buf.length ) {
                    char cr = buf[off + len];
                    if( cr == ';' ){
                        if( stack == 0 ){
                            break;
                        }
                    } else if( cr == '<' ){
                        stack ++ ;
                    } else if( cr == '>' ){
                        stack --;
                    }
                    len ++ ;
                }
                return new CatTypeDesc(true, new String(buf, off + 1, len - 1));
            // case '(':
            default:
                return new CatTypeDesc(false, new String(buf, off, buf.length - off));
        }
    }
    
    
    private static class CatTypeDesc {
        private int off = 0;
        private String descriptor;
        private String full;
        private int length;

        public CatTypeDesc(Type type, Type full) {
            this.descriptor = type.getDescriptor();
            this.full = full.getDescriptor();
            this.length = descriptor.length();
        }
        public CatTypeDesc(boolean objectType, String descriptor) {
            this.off = objectType ? 2 : 0;
            this.descriptor = objectType ? ("L" + descriptor + ";") : descriptor;
            this.full = this.descriptor;
            this.length = descriptor.length();
        }
    }

}
