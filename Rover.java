import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rover extends Thread {

    public static  byte[] msg = new byte[124];
    public static byte[] msg_send;
    public static String multicastAddess; // multicast Address
    public MulticastSocket msocket = null;
    static long source_id;
   static List<String> table_id = new ArrayList<>();
   static HashMap<Integer,List<Integer>> SourceEntries = new HashMap<>();// the main entry in the routing table for the rover itself
    public static int rover_id ; // rover_id of this rover
    public static Map routeMap = new HashMap<String,List<String>>(); // HashMap to store the routing table
    public static ArrayList<Byte> by = new ArrayList<>(); // arraylist to dynamically build the array.
    public static boolean[] currentEntries = new boolean[]{false,false,false,false,false,false,false,false,false,false,false}; // store the received rover id's
    public static String[] addresses = new String[]{"0","0","0","0","0","0","0","0","0","0","0"}; // store the address of the respective rover_id index
    public static int port ;// port to send and receive.
    public boolean flag1;
    public InetAddress group;
    private static boolean change = false;// boolean value to check if the routing table has changed.

    public Rover(boolean flag){
        flag1 = flag; // initiates the sender or the receiver thread.


    }

    public static long binTodec(int k,int count){
        /**
         * Converts the binary value into decimal value
         * @return: returns the decimal value.
         */
        int  j = 15;
        long a=0;
        if (k == 2) {
            // for converting 2 bytes into decimal
            for (int i = 7; i >= 0; i--) {
                if ((msg[count] & (1 << i)) != 0)
                    a += Math.pow(2, j);
                j -= 1;

            }
            count += 1;
            for (int i = 7; i >= 0; i--) {
                if ((msg[count] & (1 << i)) != 0)
                    a += Math.pow(2, j);
                j -= 1;
            }
            return a;
        }
        else if (k == 1){
            // for converting 1 byte into decimal
            j= 7;
            for (int i = 7; i >= 0; i--) {
                if ((msg[count] & (1 << i)) != 0)
                    a += Math.pow(2, j);
                j -= 1;
            }
            return a;
        }
        else if (k == 0){
            // for converting half a byte into decimal
            j = 3;
            for (int i = 7;i >=4; i--){
                if ((msg[count] &(1 << i)) != 0){
                    a += Math.pow(2,j);
                    j -= 1;
                }
            }
            return a;
        }
        else {
            // for converting 4 bytes into decimal
            j = 31;
            for (int i = 7; i>=0; i--){
                if ((msg[count] & (1 << i)) != 0)
                    a += Math.pow(2, j);
                j -= 1;
            }
            count += 1;
            for (int i = 7; i>=0; i--){
                if ((msg[count] & (1 << i)) != 0)
                    a += Math.pow(2, j);
                j -= 1;
            }
            count += 1;
            for (int i = 7; i>=0; i--){
                if ((msg[count] & (1 << i)) != 0)
                    a += Math.pow(2, j);
                j -= 1;
            }
            count += 1;
            for (int i = 7; i>=0; i--){
                if ((msg[count] & (1 << i)) != 0)
                    a += Math.pow(2, j);
                j -= 1;
            }
            return a;
        }
    }
    public static String[] createString(String address){
        /**
         * This mehhod creates a string  array from a given string by splitting the string
         * on '.' .
         * returns the IP address separated in a array.
         */
        String[] s = new String[4];
        if (address.contains("."))
            s = address.split("\\.");
        else{
            String[] s2 = new String[1];
            s2[0] = address;
            return s2;
        }
        return s;
    }

    public static void createMessage(){
        /**
         * This method creates the RIP data packet from the routing table.
         */
        int count = 0;
        long i = 1;
        // this part adds the entry of the current rover itself as the
        // first entry in the RIP packet
        List<String> table = new ArrayList<>();
        by.set(count++,(byte)2);
        by.set(count++,(byte)2);
        by.set(count++,(byte)routeMap.size()); // to mention the number of entries the RIP packet.
        by.set(count++,(byte)0);
        String[] s = createString(table_id.get(0));
        by.set(count++, (byte) 0);
        by.set(count++, (byte) 2);
        by.set(count++, (byte) 0);
        by.set(count++, (byte) 0);
        // the destination address
        by.set(count++, (byte) Integer.parseInt(s[0]));
        by.set(count++, (byte) Integer.parseInt(s[1]));
        by.set(count++, (byte) Integer.parseInt(s[2]));
        by.set(count++, (byte) Integer.parseInt(s[3]));
        // subnet Mask
        by.set(count++, (byte) 255);
        by.set(count++, (byte) 0);
        by.set(count++, (byte) 0);
        by.set(count++, (byte) 0);
        s = createString(table_id.get(1));
        // Next Hop IP address
        by.set(count++, (byte) Integer.parseInt(s[0]));
        by.set(count++, (byte) Integer.parseInt(s[1]));
        by.set(count++, (byte) Integer.parseInt(s[2]));
        by.set(count++, (byte) Integer.parseInt(s[3]));
        by.set(count++, (byte) 0);
        by.set(count++, (byte) 0);
        by.set(count++, (byte) 0);
        // Metric for the given entry will always be 0
        by.set(count++, (byte) Integer.parseInt(table_id.get(2)));
        // Loop through the routing table to get all entries and
        // put them in the RIP packet.
        while(i < 10) {
            if (i != rover_id) {
                if (routeMap.containsKey(i)) {
                    table = (List<String>) routeMap.get(i);
                    s = createString(table.get(0));
                    by.set(count++, (byte) 0);
                    by.set(count++, (byte) 2);
                    by.set(count++, (byte) 0);
                    by.set(count++, (byte) 0);
                    by.set(count++, (byte) Integer.parseInt(s[0]));
                    by.set(count++, (byte) Integer.parseInt(s[1]));
                    by.set(count++, (byte) Integer.parseInt(s[2]));
                    by.set(count++, (byte) Integer.parseInt(s[3]));
                    by.set(count++, (byte) 255);
                    by.set(count++, (byte) 255);
                    by.set(count++, (byte) 255);
                    by.set(count++, (byte) 252);
                    s = createString(table.get(1));
                    by.set(count++, (byte) Integer.parseInt(s[0]));
                    by.set(count++, (byte) Integer.parseInt(s[1]));
                    by.set(count++, (byte) Integer.parseInt(s[2]));
                    by.set(count++, (byte) Integer.parseInt(s[3]));
                    by.set(count++, (byte) 0);
                    by.set(count++, (byte) 0);
                    by.set(count++, (byte) 0);
                    by.set(count++, (byte) Integer.parseInt(table.get(2)));
                    i++;
                } else
                    i++;
            }else{
                i ++;
            }
        }


    }

    public static void makeRoutingTable2(long id,String address, String cost,InetAddress packetSource, String dest){
        /**
         * Method to update the routing table after receiving a packet
         * it takes the rover id of the destination, address of the destination,
         * cost to the destination, and the nextHop addresss
         */
        List<Integer> dests = new ArrayList<>();
        // this sets the cost to 1 if it is 0
        // also initialies the source_id for later use.
        if(!addresses[rover_id].equals(dest)) {
            if (cost.equals("0")) {
                cost = "1";
                currentEntries[(int) id] = true;
                // to remember the id of the current packet sender
                source_id = id;
                if (addresses[(int) id].equals("0"))
                    addresses[(int) id] = packetSource.toString().substring(1);
            }
            List<String> table = new ArrayList<>();
            List<String> table1 = new ArrayList<>();
            if (routeMap.containsKey(id)) {
                table = (List<String>) routeMap.get(id);
                // to see if the current entry is about the source id itself
                if (packetSource.toString().substring(1).equals(addresses[(int) id])) {
                    if (!cost.equals(table.get(2))) {
                        change = true;
                        table.set(0, address);
                        table.set(1, String.valueOf(packetSource).substring(1));
                        table.set(2, cost);
                        routeMap.put(id, table);
                        dests.add((int)source_id);
                        SourceEntries.put((int)source_id,dests);
                    }
                } else {
                    // to get the cost between this rover and the rover that has sent this packet from the routing table
                    table1 = (List<String>) routeMap.get(source_id);
                    // to check whether the newCost from this rover is less than the cost present in the routing table
                    String newCost = String.valueOf(Integer.parseInt(table1.get(2)) + Integer.parseInt(cost));
                    if (Integer.parseInt(table.get(2)) > Integer.parseInt(newCost)) {
                        change = true;
                        table.set(0, address);
                        table.set(1, String.valueOf(packetSource).substring(1));
                        table.set(2, cost);
                        routeMap.put(id, table);
                        dests.add((int)id);
                        SourceEntries.put((int)source_id,dests);

                    }
                }
            } else {
                // to check if the entry is not for the source and if it has not been previously visited
                if ((!packetSource.toString().substring(1).equals(addresses[(int) id])) && !currentEntries[(int) id]) {
                    change = true;
                    table = (List<String>) routeMap.get(source_id);
                    String new_cost = String.valueOf(Integer.parseInt(table.get(2)) + Integer.parseInt(cost));
                    table1.add(0, address);
                    table1.add(1, String.valueOf(packetSource).substring(1));
                    table1.add(2, new_cost);
                    routeMap.put(id, table1);
                    dests.add((int)id);
                    SourceEntries.put((int)source_id,dests);
                }
                // if the entry is for the source then just add
                else if (packetSource.toString().substring(1).equals(addresses[(int) id])) {
                    change = true;
                    table.add(0, address);
                    table.add(1, String.valueOf(packetSource).substring(1));
                    table.add(2, cost);
                    routeMap.put(id, table);
                    dests.add((int)id);
                    SourceEntries.put((int)source_id,dests);
                }
            }
        }

    }
    public static void removeEntrise(){
        /**
         * This methods removes those entries that
         * no longer provide a route to a destination
         * present in the routing table.
         */
        List<Integer> dests = new ArrayList<>();
        List<Integer> dests2= new ArrayList<>();
        for(int i = 0; i < 10;i++){
            if(SourceEntries.containsKey(i)){
                dests =SourceEntries.get(i);
                for( int j = 0 ;j < 10;j ++){
                    if (routeMap.containsKey(i)){
                        if (!SourceEntries.containsKey((long)i)){
                            routeMap.remove(i);
                            change = true;
                        }

                    }
                }

            }
        }
    }

    public static void checkEntries() throws UnknownHostException {
        for (int i =1;i < currentEntries.length;i++){
            if (!addresses[i].equals(InetAddress.getLocalHost().getHostAddress()))
            if (!currentEntries[i]){
                if (routeMap.containsKey((long)i)) {
                    List<String> table = (List<String>) routeMap.get((long) i);
                    if (table.get(2).equals("1"))
                        routeMap.remove((long) i);
                    change = true;
                }
            }
            currentEntries[i] = false;
        }
    }

    public void run(){
        /**
         * the method that receives the message or sends the message depending
         * on the parameters provided
         */
        try {

            msocket = new MulticastSocket(port);
            group = InetAddress.getByName(multicastAddess);
            msocket.joinGroup(group);
            if (flag1) {
                while(true) {
                    DatagramPacket packet = new DatagramPacket(msg, msg.length);
                    msocket.receive(packet);
                    // To avoid getting a packet from itself
                    // we check the IP address of the packet and comapare
                    // it with the local host
                   if(!InetAddress.getLocalHost().getHostAddress().equals(packet.getAddress().toString().substring(1))) {
                        InetAddress packetSource = packet.getAddress();
                        // analyze the received data and get the required values
                        decode(packetSource);

                    }
                }

            }
            else{
                // create a new byte array that is of the length of
                // the updated values of the routing table
                    createByteArray(routeMap.size());
                    // create the data that is to be sent
                    createMessage();
                    System.out.println();
                    // print the routing table
                    printRoutingTable();
                    // send the byte array
                    msg_send = new byte[by.size()];
                    for (int i = 0; i < by.size(); i++) {
                        msg_send[i] = by.get(i);
                    }
                    System.out.println();
                    group = InetAddress.getByName("224.0.0.0");
                    DatagramPacket packet = new DatagramPacket(msg_send, msg_send.length, group, port);
                    msocket.send(packet);





            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void decode( InetAddress packetSource){
        /**
         * Decodes the packet data and retrieves the required values
         */
        // initialize a index counter to traverse the packet data
        int count = 0;
        count += 2;
        // calculate the number of entries that will be present in the packet.
        int numbero_of_entries;
        numbero_of_entries = (int)binTodec(1,count);
        count += 1;
        count +=1;
        String dest_address = "";
        // this loop is to
        // calculate all the values of the packet entries
        for (int i = 1; i<=numbero_of_entries;i++) {
            count += 4;
            String address = "";
            // Getting the Multicast Address of the destination
            address += String.valueOf(binTodec(1, count)) + ".";
            count += 1;
            address += String.valueOf(binTodec(1, count)) + ".";
            // determining the id of the destination rover.
            long id = binTodec(1, count);
            count += 1;
            address += String.valueOf(binTodec(1, count)) + ".";
            count += 1;
            address += String.valueOf(binTodec(1, count));
            count += 5;
            // determining the destination rover of the received entry.
            dest_address += String.valueOf(binTodec(1, count)) + ".";
            count += 1;
            dest_address += String.valueOf(binTodec(1, count)) + ".";
            count += 1;
            dest_address += String.valueOf(binTodec(1, count)) + ".";
            count += 1;
            dest_address += String.valueOf(binTodec(1, count));
            count += 4;
            // get the cost/metric of the path.
            String cost = String.valueOf(binTodec(1, count));
            count += 1;
            // method to create the routing table from the values.
            makeRoutingTable2(id, address, cost, packetSource, dest_address);
        }
    }

    public static void createByteArray(int number_of_entries){
        /**
         * Changes the size of the dynamic byte array according to the
         * size of the routing table.
         */
        // initilaze the new size of the routing table
        int newsize = 4 + (20 * number_of_entries);
        // check if the current size is less than the new size
        // if yes then add the difference in size to the byte array list
        if (by.size() < newsize){
            int addExtra = newsize - by.size();
            for(int i =0;i < addExtra;i++){
                by.add((byte)0);
            }
        }
    }

    public static void printRoutingTable(){
        /**
         * Prints the routing table if any update is made in it
         */

        // if the routing table was updated print the table
        // reset the value of the flag.
        if (change){
            System.out.println("Address\t\tNext Hop\t\tCost");
            for(int i =1;i<=10;i++) {
                if (routeMap.containsKey((long)i)) {
                    List<String> table = (List<String>) routeMap.get((long)i);
                    System.out.println(table.get(0) + "\t" + table.get(1) + "\t" + table.get(2));
                }
            }
            change = false;
        }

    }

    public static void main(String[] args) throws InterruptedException, IOException {
        // initialize the byte array list
        for (int i = 0;i < 24;i ++){
            by.add((byte)0);
        }
        // take the command line values and
        multicastAddess = args[0];
        port = Integer.parseInt(args[1]);
        rover_id = Integer.parseInt(args[2]);
        addresses[rover_id] = InetAddress.getLocalHost().getHostAddress();
        // assigns a multicast IP
        table_id.add("10." + rover_id + ".0." + "0");
        InetAddress a = InetAddress.getLocalHost();
        table_id.add(String.valueOf(a.getHostAddress()));
        table_id.add("0");
        routeMap.put((long)rover_id,table_id);
        change = true;
        printRoutingTable();
        Rover receive = new Rover(true);
        Rover send = new Rover(false);
        receive.start();
        int count = 0;
        // timer for sending and checking of messages.
        while (true){
            sleep(5000);
            send.run();
            if (count == 0 ){
                count =1;
            }
            if (count == 1){
                removeEntrise();
                checkEntries();
                count = 0;
            }
       }







    }
}
