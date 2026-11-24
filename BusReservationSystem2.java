import java.io.*;
import java.util.*;

class Bus {
    String busNumber;
    String route;
    int fare;
    int totalSeats;
    int bookedSeats;

    Queue<String> waitingQueue = new LinkedList<>();

    Bus(String busNumber, String route, int fare, int totalSeats) {
        this.busNumber = busNumber;
        this.route = route;
        this.fare = fare;
        this.totalSeats = totalSeats;
        this.bookedSeats = 0;
    }

    int availableSeats() {
        return totalSeats - bookedSeats;
    }
}

class Booking {
    String ticketId;
    String passengerName;
    String busNumber;

    Booking(String ticketId, String passengerName, String busNumber) {
        this.ticketId = ticketId;
        this.passengerName = passengerName;
        this.busNumber = busNumber;
    }
}

public class BusReservationSystem2 {

    static Scanner sc = new Scanner(System.in);

    static ArrayList<Bus> buses = new ArrayList<>();
    static ArrayList<Booking> bookings = new ArrayList<>();

    static int nextTicket = 101;

    // Create sample bus file if it does not exist
    static void createBusFile() {
        File file = new File("buses.txt");

        if (!file.exists()) {
            try {
                FileWriter fw = new FileWriter(file);

                fw.write("B101,Raipur-Bhopal,700,2\n");
                fw.write("B201,Raipur-Nagpur,450,2\n");

                fw.close();
            } catch (IOException e) {
                System.out.println("Error creating bus file.");
            }
        }
    }

    // Load buses from file
    static void loadBuses() {
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader("buses.txt"));

            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                buses.add(new Bus(
                        data[0],
                        data[1],
                        Integer.parseInt(data[2]),
                        Integer.parseInt(data[3])
                ));
            }

            br.close();

        } catch (IOException e) {
            System.out.println("Error loading buses.");
        }
    }

    // Find bus using Linear Search
    static Bus findBus(String number) {
        for (Bus b : buses) {
            if (b.busNumber.equalsIgnoreCase(number)) {
                return b;
            }
        }

        return null;
    }

    // Load previous bookings
    static void loadBookings() {
        File file = new File("bookings.txt");

        if (!file.exists()) {
            return;
        }

        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(file));

            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (data[0].equals("NEXT")) {
                    nextTicket = Integer.parseInt(data[1]);
                } else if (data.length == 3) {
                    Bus b = findBus(data[2]);

                    if (b != null && b.availableSeats() > 0) {
                        bookings.add(new Booking(
                                data[0], data[1], data[2]));

                        b.bookedSeats++;
                    }
                }
            }

            br.close();

        } catch (IOException e) {
            System.out.println("Error loading bookings.");
        }
    }

    // Save bookings to file
    static void saveBookings() {
        try {
            FileWriter fw = new FileWriter("bookings.txt");

            fw.write("NEXT," + nextTicket + "\n");

            for (Booking b : bookings) {
                fw.write(b.ticketId + ","
                        + b.passengerName + ","
                        + b.busNumber + "\n");
            }

            fw.close();

            System.out.println("Data saved successfully.");

        } catch (IOException e) {
            System.out.println("Error saving data.");
        }
    }

    // Display all buses
    static void viewBuses() {
        System.out.println("\nBUS DETAILS");
        System.out.println("Bus ID\tRoute\t\tFare\tSeats Left");

        for (Bus b : buses) {
            System.out.println(b.busNumber + "\t"
                    + b.route + "\t"
                    + b.fare + "\t"
                    + b.availableSeats());
        }
    }

    // Search bus by destination
    static void searchBus() {
        sc.nextLine();

        System.out.print("Enter destination: ");
        String destination = sc.nextLine();

        boolean found = false;

        for (Bus b : buses) {
            if (b.route.toLowerCase().contains(
                    destination.toLowerCase())) {

                System.out.println(b.busNumber + " "
                        + b.route + " Fare: " + b.fare
                        + " Available Seats: " + b.availableSeats());

                found = true;
            }
        }

        if (!found) {
            System.out.println("No bus found!");
        }
    }

    // Book ticket
    static void bookTicket() {
        sc.nextLine();

        System.out.print("Enter bus number: ");
        String number = sc.nextLine();

        Bus b = findBus(number);

        if (b == null) {
            System.out.println("Bus " + number + " not found.");
            return;
        }

        System.out.print("Enter passenger name: ");
        String name = sc.nextLine();

        if (b.availableSeats() > 0) {
            addBooking(name, b);
        } else {
            b.waitingQueue.add(name);

            System.out.println("Bus full. "
                    + name + " added to waiting queue.");
        }
    }

    // Add a confirmed booking
    static void addBooking(String name, Bus b) {
        String id = "TKT" + nextTicket;

        bookings.add(new Booking(id, name, b.busNumber));

        b.bookedSeats++;
        nextTicket++;

        System.out.println("Ticket booked: " + id
                + " | " + name + " | " + b.busNumber
                + " " + b.route);

        System.out.println("Seats left: " + b.availableSeats());
    }

    // Display all bookings
    static void viewBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings available.");
            return;
        }

        System.out.println("\nTICKET ID\tPASSENGER\tBUS");

        for (Booking b : bookings) {
            System.out.println(b.ticketId + "\t"
                    + b.passengerName + "\t"
                    + b.busNumber);
        }
    }

    // Cancel ticket and assign seat to waiting passenger
    static void cancelTicket() {
        sc.nextLine();

        System.out.print("Enter ticket ID: ");
        String id = sc.nextLine();

        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);

            if (b.ticketId.equalsIgnoreCase(id)) {
                Bus bus = findBus(b.busNumber);

                bookings.remove(i);
                bus.bookedSeats--;

                System.out.println(id + " cancelled.");

                if (!bus.waitingQueue.isEmpty()) {
                    String name = bus.waitingQueue.poll();

                    System.out.println("Seat given to " + name);

                    addBooking(name, bus);
                }

                return;
            }
        }

        System.out.println("Ticket not found.");
    }

    // Sort buses by fare using Bubble Sort
    static void sortBuses() {
        for (int i = 0; i < buses.size() - 1; i++) {
            for (int j = 0; j < buses.size() - i - 1; j++) {

                if (buses.get(j).fare > buses.get(j + 1).fare) {
                    Bus temp = buses.get(j);

                    buses.set(j, buses.get(j + 1));
                    buses.set(j + 1, temp);
                }
            }
        }

        System.out.println("\nBUSES SORTED BY FARE");

        viewBuses();
    }

    // Main method
    public static void main(String[] args) {

        createBusFile();
        loadBuses();
        loadBookings();

        int choice;

        do {
            System.out.println("\n===== BUS RESERVATION SYSTEM =====");
            System.out.println("1. View Buses");
            System.out.println("2. Search Bus by Destination");
            System.out.println("3. Book Ticket");
            System.out.println("4. Cancel Ticket");
            System.out.println("5. Sort Buses by Fare");
            System.out.println("6. View Bookings");
            System.out.println("7. Exit");

            System.out.print("Enter your choice: ");
            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    viewBuses();
                    break;

                case 2:
                    searchBus();
                    break;

                case 3:
                    bookTicket();
                    break;

                case 4:
                    cancelTicket();
                    break;

                case 5:
                    sortBuses();
                    break;

                case 6:
                    viewBookings();
                    break;

                case 7:
                    saveBookings();
                    System.out.println("Thank you!");
                    break;

                default:
                    System.out.println("Invalid choice!");
            }

        } while (choice != 7);

        sc.close();
    }
}