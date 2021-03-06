import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ErrorSimConnection extends Thread{

    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket  socket,fakeSocket;
    private int serverPort=69;
    private int portToSend;
    private boolean flip=false;
    private int clientsTID;
    private int serversTID;
    private static int modified;
    private int fileNameLen;
    private String fileName;
    private byte[] data;
    private int usingFakeSocket=0;
    private boolean resend=false;
    private int modifiedPackIndex=2;
    private int delay=0;
    private String addToSend;
    private InetAddress addressToSend=null;
    public ErrorSimConnection(DatagramPacket pack,int modified,int modifiedPackIndex,int delay,String addToSend) {
        this.modified=modified;
        this.addToSend=addToSend;
        this.modifiedPackIndex=modifiedPackIndex;
        this.delay=delay;
        try {
            socket = new DatagramSocket();
            //ipAddr = socket.getLocalAddress().getHostAddress();

        }
        catch (SocketException se){
            se.printStackTrace();
            System.exit(1);
        }
        receivePacket=pack;
    }

    public void run() {

        clientsTID=receivePacket.getPort();
        data=receivePacket.getData();
        byte newdata[] = new byte[receivePacket.getLength()];
        for(int i =0;i<newdata.length;i++)
        {
            newdata[i]=data[i];
        }
        data=newdata;
        System.out.println("Packet Length is: " +data.length);

        printInfoReceived(receivePacket,data);
        System.out.println();




        while(true) {

            if(modified==611)//lose RQ
            {
                if(data[1]==1||data[1]==2) {
                    System.out.println("lost RQ");

                    modified=0;

                    receive();

                    continue;
                }
            }

            if(modified==612)//lose data block
            {
                if(data[1]==3)
                {
                    if(modifiedPackIndex>=10)
                    {
                        if(data[2]==modifiedPackIndex/10&&data[3]==modifiedPackIndex%10)
                        {
                            System.out.println("lose data block "+modifiedPackIndex);
                            receive();
                            modified=0;
                            //continue;
                        }
                    }
                    else if(modifiedPackIndex<10)
                    {
                        if(data[3]==modifiedPackIndex) {
                            System.out.println("lose data block "+modifiedPackIndex);
                            receive();
                            modified=0;
                            //continue;
                        }
                    }
                    /*if(flip)
                        flip=false;
                    else
                        flip=true;*/
                }
            }
            if(modified==613)//lose ACK
            {
                if(data[1]==4)
                {
                    if(modifiedPackIndex>=10)
                    {
                        if(data[2]==modifiedPackIndex/10&&data[3]==modifiedPackIndex%10)
                        {
                            System.out.println("lose ACK "+modifiedPackIndex);
                            receive();
                            modified=0;
                            if(flip)
                                flip=false;
                            else
                                flip=true;
                            continue;
                        }
                    }
                    else if(modifiedPackIndex<10)
                    {
                        if(data[3]==modifiedPackIndex) {
                            System.out.println("lose ACK "+modifiedPackIndex);
                            receive();
                            modified=0;
                            if(flip)
                                flip=false;
                            else
                                flip=true;
                            continue;
                        }
                    }

                }
            }
            if(modified==631)//duplicate RQ
            {
                if(data[1]==1||data[1]==2) {
                    System.out.println("duplicate RQ");
                    resend=true;
                }
            }
            if(modified==632)//duplicate data block
            {
                if(data[1]==3)
                {
                    if(modifiedPackIndex>=10)
                    {
                        if(data[2]==modifiedPackIndex/10&&data[3]==modifiedPackIndex%10)
                        {
                            System.out.println("duplicate data block "+modifiedPackIndex);
                            resend=true;
                        }
                    }
                    else if(modifiedPackIndex<10)
                    {
                        if(data[3]==modifiedPackIndex) {
                            System.out.println("duplicate data block "+modifiedPackIndex);
                            resend=true;
                        }
                    }
                }
            }
            if(modified==633)//duplicate ACK
            {
                if(data[1]==4)
                {
                    if(modifiedPackIndex>=10)
                    {
                        if(data[2]==modifiedPackIndex/10&&data[3]==modifiedPackIndex%10)
                        {
                            System.out.println("duplicate ACK "+modifiedPackIndex);
                            resend=true;
                        }
                    }
                    else if(modifiedPackIndex<10)
                    {
                        if(data[3]==modifiedPackIndex) {
                            System.out.println("duplicate ACK "+modifiedPackIndex);
                            resend=true;
                        }
                    }
                }
            }



            if(receivePacket.getPort()==clientsTID)
            {
                if (modified == 411) {
                    {
                        if(data[1]==(byte)1||data[1]==(byte)2)
                            data = badOpCode();
                    }
                }  else if (modified == 421) {
                    if (data[1] == (byte) 3) {
                        data = badOpCode();
                    }
                } else if (modified == 422) {
                    if (data[1] == (byte) 3) {
                        data[3] = (byte) 9;
                    }
                } else if (modified == 431) {
                    if (data[1] == (byte) 4) {
                        data = badOpCode();
                    }
                } else if (modified == 432) {
                    if (data[1] == (byte) 4) {
                        data[3] = (byte) 9;
                    }
                }
                else if (modified == 412) {
                    data = badMode();

                     modified=0;
             }
            }
            

                if (serversTID == 0) {
                    if(addToSend.compareTo("")==0)
                    {

                        try {
                            addressToSend=InetAddress.getLocalHost();
                        }
                        catch (Exception e)
                        {System.out.println("There is an error with the IP address");}
                    }
                    else {
                        try {
                            System.out.println("Sending to IP address: "+addToSend);
                            addressToSend = InetAddress.getByName(addToSend);
                        } catch (Exception e) {
                            System.out.println("There is an error with the IP address");
                        }
                    }
                    portToSend = 69;
                }
                else if (receivePacket.getPort()==clientsTID) {
                    System.out.println("sending to server");
                    portToSend = serversTID;
                    flip = false;
                    if(addToSend.compareTo("")==0)
                    {
                        try {
                            addressToSend=InetAddress.getLocalHost();
                        }
                        catch (Exception e)
                        {System.out.println("There is an error with the IP address");}
                    }
                    else {
                        try {
                            addressToSend = InetAddress.getByName(addToSend);
                        } catch (Exception e) {
                            System.out.println("There is an error with the IP address");
                        }
                    }

                } else {
                    System.out.println("sending to client");
                    flip = true;
                    portToSend = clientsTID;
                    try {
                    addressToSend= InetAddress.getLocalHost();
                    }
                    catch (Exception e)
                    {}
                }


            /*try {
                addressToSend= InetAddress.getLocalHost();
            }
            catch (Exception e)
            {}*/


               /* try {
                    addressToSend=InetAddress.getByName("134.117.59.205");
                }
                catch (Exception e)
                {}*/

                sendPacket = new DatagramPacket(data, data.length,
                        addressToSend, portToSend);

                printInfoToSend(sendPacket);

            // or (as we should be sending back the same thing)
            // System.out.println(received);


            if(modified==621)  //delay RQ
            {
                if(data[1]==1||data[1]==2)
                {
                    System.out.print("dalay RQ for "+delay+" seconds");
                    delay(delay);
                    modified=0;
                }
            }
            if(modified==622)   //delay data block
            {
                if(data[1]==3)
                {
                    if(modifiedPackIndex>=10)
                    {
                        if(data[2]==modifiedPackIndex/10&&data[3]==modifiedPackIndex%10)
                        {
                            System.out.print("dalay Data block "+modifiedPackIndex+" for "+delay+" seconds");
                            delay(delay);
                            modified=0;
                        }
                    }
                    else if(modifiedPackIndex<10)
                    {
                        if(data[3]==modifiedPackIndex) {
                            System.out.print("dalay Data block " + modifiedPackIndex + " for "+delay+" seconds.");
                            delay(delay);
                            modified=0;
                        }
                    }
                }
            }
            if(modified==623)    //delay ACK
            {
                if(data[1]==4)
                {
                    if(modifiedPackIndex>=10)
                    {
                        if(data[2]==modifiedPackIndex/10&&data[3]==modifiedPackIndex%10)
                        {
                            System.out.print("dalay ACK "+modifiedPackIndex+" for "+delay+" seconds");
                            delay(delay);
                            modified=0;
                        }
                    }
                    else if(modifiedPackIndex<10)
                    {
                        if(data[3]==modifiedPackIndex) {
                            System.out.print("dalay ACK " + modifiedPackIndex + " for "+delay+" seconds");
                            delay(delay);
                            modified=0;
                        }
                    }
                }
            }

                // Send the datagram packet to the client via the send socket.
               // System.out.println("modified= " + modified + " usingFakeSocket=" + usingFakeSocket);
                if (modified == 5 && usingFakeSocket >= 2) {
                    System.out.println("Using the fake Socket");
                    try {
                        fakeSocket = new DatagramSocket();
                        fakeSocket.send(sendPacket);

                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                } else {
                    try {
                        if (resend) {
                            System.out.println("duplicate package sent");
                            socket.send(sendPacket);
                            delay(delay);
                            resend = false;
                        }
                        socket.send(sendPacket);
                        usingFakeSocket++;
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                System.out.println("ErrorSimulator: packet sent");
                if (data[1] == (byte) 0) {

                    break;
                }

                receive();
                if (serversTID == 0)
                    serversTID = receivePacket.getPort();

        }
        System.out.println("Host thread shuts down");




    }
    public void delay(int s)
    {
        try
        {
            for(int i=0;i<s;i++) {
                System.out.print(".");
                Thread.sleep( 1000);

            }
            System.out.println("Thread awake");
        }
        catch (InterruptedException e)
        {}
    }
    private void receive()
    {
        System.out.println();
        data = new byte[516];

        receivePacket = new DatagramPacket(data,data.length);


        try {
            System.out.println("we are now waiting");
            socket.receive(receivePacket);

        }
        catch(IOException e) {
            System.out.println("IOException on waiting");
            System.exit(1);
        }
        byte newdata[] = new byte[receivePacket.getLength()];
        for(int i =0;i<newdata.length;i++)
        {
            newdata[i]=data[i];
        }
        data=newdata;
    }
    //Additional Helper Functions
    //IO DISPLAY FUNCTIONS
    private void printInfoToSend(DatagramPacket pack) {
        System.out.println("InterHost: Sending packet:");

        int len = getLen(pack);
      
        System.out.print("Containing: ");
    
        for(int x = 0;x<len;x++) {
            System.out.print(pack.getData()[x]);
        }
        System.out.println("");
    }
    private void printInfoReceived(DatagramPacket pack,byte[] dataByte) {
        System.out.println("InterHost: Packet received:");
     
        int len = getLen(pack);
      
        System.out.print("Containing: ");
      

        
        System.out.print("Package in bytes: ");
        for(int x = 0;x<len;x++) {
            System.out.print(pack.getData()[x]);
        }
        System.out.println("");
        String receive = new String(pack.getData(),0,pack.getLength());
        System.out.print("Package in string: "+ receive);



    }


    private int getLen(DatagramPacket pack) {
        return pack.getLength();
    }
    private byte[] badOpCode() {
        byte[] badOpCodeBytes = new byte[receivePacket.getLength()];
        badOpCodeBytes = receivePacket.getData();
        badOpCodeBytes[0] = 1;
        badOpCodeBytes[1] = 1;
        System.out.println("OPCODE MODIFIED");
     
        return badOpCodeBytes;

    }
    private byte[] badMode() {
        parseData();
        byte[] fileNameBytes = fileName.getBytes();
        byte[] oldInfo = new byte[fileNameLen + 2 + 1 + 1 + 8];
        oldInfo[0] = 0;
        oldInfo[1] = (byte) getOpcode();
        for (int i = 0; i < fileNameLen; i++) {
            oldInfo[i + 2] = fileNameBytes[i];
        }
        oldInfo[2 + fileNameLen] = (byte) 0;
        byte[] nebasciiBytes = "nebascii".getBytes();
        for (int i = 0; i < nebasciiBytes.length; i++) {
            oldInfo[i + 2 + 1 + fileNameLen] = nebasciiBytes[i];
        }
        oldInfo[2 + fileNameLen + 1 + 8] = (byte) 0;
        System.out.print("MODIFIED MODE = ");
        for (int x = 0; x < oldInfo.length; x++) {
            System.out.print(oldInfo[x]);
        }
        System.out.print("");

        return oldInfo;
    }
    private int getOpcode()
    {
        return receivePacket.getData()[1];

    }
    private void parseData() {
        int i  = 2;
        int len = 2;
        // Figures out how long the filename is
        while(receivePacket.getData()[i] != (byte)0)
        {
            len++;
            i++;
        }
        fileName = new String(receivePacket.getData(),2,len-2);
        fileNameLen = fileName.length();
    }
}