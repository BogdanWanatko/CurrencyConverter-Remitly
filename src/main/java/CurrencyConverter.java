import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.*;

public class CurrencyConverter {
    public static double convert(String amount, String startCurrency, String endCurrency) throws IOException, JSONException {
        double exchangedValue;
        String nbpApiUrl;
        String jsonString;

        // Check if the initial currency is PLN
        if (startCurrency.equals("PLN")) {
            nbpApiUrl = "http://api.nbp.pl/api/exchangerates/rates/a/" + endCurrency + "/?format=json";
        } else if (endCurrency.equals("PLN")) {
            // Reverse the exchange direction for PLN
            nbpApiUrl = "http://api.nbp.pl/api/exchangerates/rates/a/" + startCurrency + "/?format=json";
        } else {
            nbpApiUrl = "http://api.nbp.pl/api/exchangerates/rates/a/" + startCurrency + "/?format=json";
            URL url = new URL(nbpApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int statusCode = connection.getResponseCode();
            if (statusCode == 404) {
                throw new JSONException("Currency not found: " + startCurrency);
            }
            Scanner scanner = new Scanner(url.openStream());
            jsonString = scanner.nextLine();
            scanner.close();
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray ratesArray = jsonObject.getJSONArray("rates");
            if (ratesArray.length() == 0) {
                throw new JSONException("Currency not found: " + startCurrency);
            }
            double exchangeRate = ratesArray.getJSONObject(0).getDouble("mid");
            exchangedValue = Double.parseDouble(amount) * exchangeRate;
            nbpApiUrl = "http://api.nbp.pl/api/exchangerates/rates/a/" + endCurrency + "/?format=json";
        }

        // Get the exchange rate from the NBP API
        URL url = new URL(nbpApiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int statusCode = connection.getResponseCode();
        if (statusCode == 404) {
            throw new JSONException("Currency not found: " + endCurrency);
        }
        Scanner scanner = new Scanner(url.openStream());
        jsonString = scanner.nextLine();
        scanner.close();
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray ratesArray = jsonObject.getJSONArray("rates");
        if (ratesArray.length() == 0) {
            throw new JSONException("Currency not found: " + endCurrency);
        }
        double exchangeRate = ratesArray.getJSONObject(0).getDouble("mid");

        // Convert the amount to double
        double amountToExchange = Double.parseDouble(amount);

        // Calculate the exchanged value
        if (startCurrency.equals("PLN")) {
            exchangedValue = amountToExchange / exchangeRate;
        } else {
            exchangedValue = amountToExchange * exchangeRate;
        }

        return exchangedValue;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the amount to exchange: ");
        String amount = scanner.nextLine();
        // Check if the amount is a valid number
        if (!amount.matches("\\d+(\\.\\d+)?")) {
            System.out.println("Invalid amount entered. Please enter a number.");
            return;
        }

        System.out.print("Enter the starting currency: ");
        String startCurrency = scanner.nextLine().toUpperCase();
        // Check if the starting currency is a valid 3-letter currency code
        if (startCurrency.length() != 3 || !startCurrency.matches("[A-Z]+")) {
            System.out.println("Invalid starting currency entered. Please enter a valid 3-letter currency code.");
            return;
        }

        System.out.print("Enter the ending currency: ");
        String endCurrency = scanner.nextLine().toUpperCase();
        // Check if the ending currency is a valid 3-letter currency code
        if (endCurrency.length() != 3 || !endCurrency.matches("[A-Z]+")) {
            System.out.println("Invalid ending currency entered. Please enter a valid 3-letter currency code.");
            return;
        }

        scanner.close();

        try {
            double exchangedValue = CurrencyConverter.convert(amount, startCurrency, endCurrency);
            // Round the exchanged value to 2 decimal places
            exchangedValue = Math.round(exchangedValue * 100.0) / 100.0;
            System.out.printf("Exchanged value: %.2f %s%n", exchangedValue, endCurrency);
        } catch (IOException | JSONException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("404")) {
                System.out.println("Currency not found: " + endCurrency);
            } else {
                System.out.println(errorMessage);
            }
        }
    }
}