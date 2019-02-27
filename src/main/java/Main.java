import io.protostuff.*;
import io.protostuff.runtime.Delegate;
import io.protostuff.runtime.RuntimeSchema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static class Structure {
        static final int I_DEFAULT = 5;
        @Tag(1)
        int i = I_DEFAULT;

        @Tag(2)
        List<Integer> xs;

        @Override
        public String toString() {
            return String.format(
                    "Structure { i = %d; xs = %s }",
                    i,
                    xs == null
                        ? "null"
                        : xs.stream()
                            .map(x -> Integer.toString(x))
                            .collect(Collectors.joining(", ")));
        }

        static final Schema<Structure> SCHEMA;
        static {
            SCHEMA = RuntimeSchema.getSchema(Structure.class);
        }
    }

    public static void main(String[] args) {
        Structure msg = Structure.SCHEMA.newMessage();

        System.out.printf("new message (should have default values): %s\n", msg);

//        msg.i = 5;
//        msg.xs = List.of(1, 2, 3);

        System.out.printf("now encoding: %s\n", msg);

        LinkedBuffer buf = LinkedBuffer.allocate(1024);

        byte[] serialized = ProtostuffIOUtil.toByteArray(msg, Structure.SCHEMA, buf);

        System.out.print("serialized: ");
        for (byte b: serialized) {
            System.out.printf("0x%02x ", b);
        }
        System.out.println();

        msg = Structure.SCHEMA.newMessage();

        ProtostuffIOUtil.mergeFrom(serialized, msg, Structure.SCHEMA);

        System.out.printf("from deserialized: %s\n", msg);

        ProtostuffIOUtil.mergeFrom(serialized, msg, Structure.SCHEMA);

        System.out.printf("from deserialized (deserialized again into same object): %s\n", msg);

        if (false) {
            msg = Structure.SCHEMA.newMessage();

            if (msg.i == Structure.I_DEFAULT) {
                System.out.println("i has default value, will deserialize empty byte[]");
                serialized = new byte[]{};
            } else {
                System.out.println("i doesn't have default, we'll try to make our own byte[]");

                byte i = (byte) (msg.i & 0x7f);

                if (i != msg.i) {
                    throw new IllegalArgumentException("i won't fit in a single varint byte, try another i");
                }

                serialized = new byte[]{0x08, i};
            }

            ProtostuffIOUtil.mergeFrom(serialized, msg, Structure.SCHEMA);

            System.out.printf("from deserialized (manually serialied): %s\n", msg);
        }
    }
}
