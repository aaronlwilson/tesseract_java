package environment;

public class PixelPlane {

    public PixelPlane() {

    }

    public Node[] buildNineTiles(int startIndex, int startX, int startY, int startZ, int rotation){

        Node[] planeNodes = new Node[(36*36)+(36*36)+(36*36)];

        int counter = 0;

        //matrix math to perform rotations on the whole grid...

        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 36; j++) {
                int x = (10*i) + startX;
                int y = (10*j) + startY;
                int z = startZ;

                planeNodes[counter] = new Node(x, y, z, counter, null);
                counter++;
            }
        }

        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 36; j++) {
                int x = (10*i) + startX;
                int y = startY;
                int z = (10*j) + startZ;

                planeNodes[counter] = new Node(x, y, z, counter, null);
                counter++;
            }
        }

        for (int i = 0; i < 36; i++) {
            for (int j = 0; j < 36; j++) {
                int x = startX;
                int y = (10*i) + startY;
                int z = (10*j) + startZ;

                planeNodes[counter] = new Node(x, y, z, counter, null);
                counter++;
            }
        }


        return planeNodes;
    }

}
