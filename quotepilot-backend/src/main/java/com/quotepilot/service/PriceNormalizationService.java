
package com.quotepilot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PriceNormalizationService {

    private final double usdToInr;

    private static final Pattern PER_QUANTITY_PATTERN =
            Pattern.compile("^per\\s+(\\d+(?:\\.\\d+)?)\\s*(.*?)$");

    private static final Set<String> VALID_PER_UNITS = Set.of(
            "",
            "box", "boxes",
            "piece", "pieces",
            "unit", "units",
            "item", "items",
            "pc", "pcs",
            "ea", "each"
    );

    public PriceNormalizationService(
            @Value("${quotepilot.currency.usd-to-inr:86.0}")
            double usdToInr) {

        if (!Double.isFinite(usdToInr) || usdToInr <= 0) {
            throw new IllegalArgumentException(
                    "USD to INR exchange rate must be positive");
        }

        this.usdToInr = usdToInr;
    }

    /**
     * Backward-compatible method.
     * Lot pricing requires the quantity overload.
     */
    public double normalizePrice(
            double price,
            String currency,
            String quotedUnit) {

        return normalizePrice(price, currency, quotedUnit, null);
    }

    /**
     * Normalizes supplier prices into INR per individual item.
     */
    public double normalizePrice(
            double price,
            String currency,
            String quotedUnit,
            Double quantity) {

        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException(
                    "Price must be finite and non-negative");
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException(
                    "Currency cannot be null or blank");
        }

        String normalizedCurrency = currency.trim();

        double priceInINR;

        if ("INR".equalsIgnoreCase(normalizedCurrency)
                || "₹".equals(normalizedCurrency)) {

            priceInINR = price;

        } else if ("USD".equalsIgnoreCase(normalizedCurrency)
                || "$".equals(normalizedCurrency)) {

            priceInINR = price * usdToInr;

        } else {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + currency);
        }

        if (quotedUnit == null || quotedUnit.isBlank()) {
            throw new IllegalArgumentException(
                    "Quoted unit cannot be null or blank");
        }

        String unit = quotedUnit.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        // Individual item pricing
        if (Set.of(
                "box", "boxes",
                "per box", "per boxes",
                "piece", "pieces",
                "per piece", "per pieces",
                "each",
                "unit", "units",
                "per unit", "per units",
                "item", "items",
                "per item", "per items",
                "pc", "pcs", "ea"
        ).contains(unit)) {

            return priceInINR;
        }

        // Dynamic per-X pricing with suffix validation
        Matcher matcher = PER_QUANTITY_PATTERN.matcher(unit);

        if (matcher.matches()) {

            double divisor = Double.parseDouble(matcher.group(1));
            String suffix = matcher.group(2).trim();

            if (!Double.isFinite(divisor) || divisor <= 0) {
                throw new IllegalArgumentException(
                        "Quoted quantity must be positive: " + quotedUnit);
            }

            if (!VALID_PER_UNITS.contains(suffix)) {
                throw new IllegalArgumentException(
                        "Unsupported quoted unit: " + quotedUnit);
            }

            return priceInINR / divisor;
        }

        // Lot / batch / bundle pricing
        if (Set.of(
                "lot", "per lot",
                "batch", "per batch",
                "bundle", "per bundle",
                "set", "per set"
        ).contains(unit)) {

            if (quantity == null
                    || !Double.isFinite(quantity)
                    || quantity <= 0) {

                throw new IllegalArgumentException(
                        "Lot pricing requires a positive quantity. "
                                + "Quoted unit: " + quotedUnit);
            }

            return priceInINR / quantity;
        }

        throw new IllegalArgumentException(
                "Unsupported quoted unit: " + quotedUnit);
    }

    public double getUsdToInr() {
        return usdToInr;
    }
}
