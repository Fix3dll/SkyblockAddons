package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class BazaarData {

    @SerializedName("success")
    private boolean success = false;
    @SerializedName("lastUpdated")
    private long lastUpdated = 0L;
    @SerializedName("products")
    private Map<String, Product> products = Map.of();

    @Getter
    public static class Product {

        @SerializedName("product_id")
        private String productId;
        @SerializedName("sell_summary")
        private List<OrderSummary> sellSummary;
        @SerializedName("buy_summary")
        private List<OrderSummary> buySummary;
        @SerializedName("quick_status")
        private QuickStatus quickStatus;

        /**
         * Best insta-buy price: lowest ask in {@code buy_summary}. O(n) linear scan
         * @return lowest {@code pricePerUnit}, or {@code -1} if empty
         */
        public double getInstaBuyPrice() {
            if (buySummary == null || buySummary.isEmpty()) return -1;
            double min = Double.MAX_VALUE;
            for (OrderSummary order : buySummary) {
                if (order.getPricePerUnit() < min) min = order.getPricePerUnit();
            }
            return min;
        }

        /**
         * Best insta-sell price: highest bid in {@code sell_summary}. O(n) linear scan
         * @return highest {@code pricePerUnit}, or {@code -1} if empty
         */
        public double getInstaSellPrice() {
            if (sellSummary == null || sellSummary.isEmpty()) return -1;
            double max = 0;
            for (OrderSummary order : sellSummary) {
                if (order.getPricePerUnit() > max) max = order.getPricePerUnit();
            }
            return max;
        }

        /**
         * Absolute spread: difference between insta-buy cost and insta-sell yield.
         * @return {@code buyPrice - sellPrice}
         */
        public double getSpread() {
            return quickStatus.buyPrice - quickStatus.sellPrice;
        }

        /**
         * Spread as a fraction of the sell price.
         * @return e.g. {@code 0.05} = 5 %
         */
        public double getSpreadPercent() {
            return quickStatus.sellPrice == 0 ? 0 : getSpread() / quickStatus.sellPrice;
        }

    }

    @Getter
    public static class OrderSummary {

        @SerializedName("amount")
        private int amount;
        @SerializedName("pricePerUnit")
        private double pricePerUnit;
        @SerializedName("orders")
        private int orders;
    }

    @Getter
    public static class QuickStatus {

        @SerializedName("productId")
        private String productId;
        @SerializedName("sellPrice")
        private double sellPrice;
        @SerializedName("sellVolume")
        private long sellVolume;
        @SerializedName("sellMovingWeek")
        private long sellMovingWeek;
        @SerializedName("sellOrders")
        private int sellOrders;

        @SerializedName("buyPrice")
        private double buyPrice;
        @SerializedName("buyVolume")
        private long buyVolume;
        @SerializedName("buyMovingWeek")
        private long buyMovingWeek;
        @SerializedName("buyOrders")
        private int buyOrders;
    }

}