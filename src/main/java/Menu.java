import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class Menu {
    private Scanner userInput;
    Repository repo;

    public Menu() {
        userInput = new Scanner(System.in);
        repo = new Repository();
    }

    public void mainMenu() {
        boolean loop = true;
        while (loop) {
            System.out.println("[1] Skriv ut kundlista");
            System.out.println("[2] Lista alla produkter per kategori");
            System.out.println("[3] Lägg till produkt i kundvagn");
            System.out.print("Skriv ditt val: ");

            String menuChoice = userInput.nextLine().trim();

            switch (menuChoice) {
                case "1":
                    showCustomerMenu();
                    break;
                case "2":
                    showAllProducts();
                    break;
                case "3":
                    addToCartMenu();
                    break;
                case "q":
                    loop = false;
                    break;
                default:
                    System.out.println("\nOgiltigt val\n");
                    break;
            }
        }
    }

    private void showAllProducts() {
        List<Shoe> shoes = repo.getAllProducts();
        for (Category c : Category.values()) {
            System.out.println(c);
            List<Shoe> result = shoes.stream().
                    filter(s -> s.getCategories().contains(c)).collect(Collectors.toList());
            result.stream().forEach(s -> System.out.println("ProduktID: " + s.getId() + ", Namn: " + s.getName()));
            if (result.isEmpty()) {
                System.out.println("Inga produkter i denna kategori.");
            }
        }
        System.out.println();
    }

    /**
     * Kan ej hantera kunder med samma namn. Använder den första kunden som kommer
     * upp i index.
     */
    private void addToCartMenu() {
        System.out.println("-- Alla kunder --");
        Map<Integer, Customer> customerMap = repo.getAllCustomers();
        customerMap.entrySet().stream().forEach(c -> System.out.println(c.getValue().getName()));

        Scanner userInput = new Scanner(System.in);
        System.out.print("\nVälj kund: ");
        String customerChoice = userInput.nextLine().trim();

        List<Customer> customers = new ArrayList<>();
        customerMap.entrySet().stream().
                filter(c -> c.getValue().getName().equalsIgnoreCase(customerChoice)).
                forEach(c -> customers.add(c.getValue()));

        if (customers.isEmpty()) {
            System.out.println("Ogiltig kund.");
        } else {
            List<Shoe> shoesInStock = repo.getAllProductsInStock();
            System.out.println("-- Produkter i lager --");
            shoesInStock.stream().forEach(System.out::println);
            System.out.print("Skriv in namnet på produkten du vill välja: ");
            String productChoice = userInput.nextLine().trim();
            List<Shoe> foundShoes = repo.getProductByName(productChoice);

            if (foundShoes.isEmpty()) {
                System.out.println("Ingen produkt hittades med namnet du angav.");
            } else if (foundShoes.size() == 1) {
                Order nonExpeditedOrder = repo.getNonExpeditedOrder(customers.get(0));

                if (nonExpeditedOrder.getShoes().isEmpty()) {
                    String result = repo.addToCart(customers.get(0).getId(), 0, foundShoes.get(0).getId());
                    if (result.equalsIgnoreCase("OK")) {
                        System.out.println("Produkten har lagts till i din order.");
                    } else {
                        System.out.println("Kunde ej lägga produkten till din order.");
                    }
                } else {
                    // Tidigare order
                    System.out.println("Du har en tidigare order.");
                    System.out.println(nonExpeditedOrder.getOrderDate().toString());
                    System.out.println("Lägg produkt i ny order?");
                    String newOrderChoice = userInput.nextLine().trim();

                    if (newOrderChoice.equalsIgnoreCase("j")) {
                        String result = repo.addToCart(customers.get(0).getId(), 0, foundShoes.get(0).getId());
                        if (result.equalsIgnoreCase("OK")) {
                            System.out.println("Produkten har lagts till i din order.");
                        } else {
                            System.out.println("Kunde ej lägga produkten till din order.");
                        }
                    } else if (newOrderChoice.equalsIgnoreCase("n")) {
                        String result = repo.addToCart(customers.get(0).getId(), nonExpeditedOrder.getId(), foundShoes.get(0).getId());
                        if (result.equalsIgnoreCase("OK")) {
                            System.out.println("Produkten har lagts till i din order.");
                        } else {
                            System.out.println("Kunde ej lägga produkten till din order.");
                        }
                    }
                }
            } else if (foundShoes.size() > 1) {
                foundShoes.stream().forEach(s -> System.out.println(s.getColour()));
                System.out.print("Välj färg: ");
                String colourChoice = userInput.nextLine().trim();
                List<Shoe> filteredChoice = foundShoes.stream().filter(s -> s.getColour().toString().
                        equalsIgnoreCase(colourChoice)).collect(Collectors.toList());
                Order nonExpeditedOrder = repo.getNonExpeditedOrder(customers.get(0));

                if (nonExpeditedOrder.getShoes().isEmpty()) {
                    String result = repo.addToCart(customers.get(0).getId(), 0, foundShoes.get(0).getId());
                    if (result.equalsIgnoreCase("OK")) {
                        System.out.println("Produkten har lagts till i din order.");
                    } else {
                        System.out.println("Kunde ej lägga produkten till din order.");
                    }
                } else {
                    // Tidigare order
                    System.out.println("Du har en tidigare order.");
                    System.out.println(nonExpeditedOrder.getOrderDate().toString());
                    System.out.println("Lägg produkt i ny order?");
                    String newOrderChoice = userInput.nextLine().trim();

                    if (newOrderChoice.equalsIgnoreCase("j")) {
                        String result = repo.addToCart(customers.get(0).getId(), 0, foundShoes.get(0).getId());
                        if (result.equalsIgnoreCase("OK")) {
                            System.out.println("Produkten har lagts till i din order.");
                        } else {
                            System.out.println("Kunde ej lägga produkten till din order.");
                        }
                    } else if (newOrderChoice.equalsIgnoreCase("n")) {
                        String result = repo.addToCart(customers.get(0).getId(), nonExpeditedOrder.getId(), foundShoes.get(0).getId());
                        if (result.equalsIgnoreCase("OK")) {
                            System.out.println("Produkten har lagts till i din order.");
                        } else {
                            System.out.println("Kunde ej lägga produkten till din order.");
                        }
                    }
                }
            }
        }
    }

    private void showCustomerMenu() {
        int customerId;
        boolean loop = true;
        while (loop) {
            System.out.print("Skriv in ditt kund id: ");

            String menuChoice = userInput.nextLine().trim();
            if (menuChoice.matches("\\d+")) {
                customerId = Integer.parseInt(menuChoice);
                loop = false;

                String result = repo.getCustomerTotalOrderValue(customerId);
                System.out.println(result);
                System.out.println();
            } else if (menuChoice.isEmpty()) {
                loop = false;
                String result = repo.getCustomerTotalOrderValue(0);
                System.out.println(result);
                System.out.println();
            } else {
                System.out.println("\nSkriv in ett positivt heltal\n");
            }
        }
    }
}
