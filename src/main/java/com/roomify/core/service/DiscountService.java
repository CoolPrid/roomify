package com.roomify.core.service;

import com.roomify.core.dto.User;
import com.roomify.core.repository.UserRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DiscountService {

    private final UserRepository userRepository;
    private final Map<String, Double> activePromoCodes;
    private final Set<String> vipUsers;

    public DiscountService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.activePromoCodes = new HashMap<>();
        this.vipUsers = new HashSet<>();

        // Initialize some promo codes
        activePromoCodes.put("WELCOME10", 0.10);  // 10% off
        activePromoCodes.put("SAVE20", 0.20);     // 20% off
        activePromoCodes.put("SUMMER25", 0.25);   // 25% off
        activePromoCodes.put("EXPIRED", 0.50);    // This one is "expired"

        // Some VIP users
        vipUsers.add("vip-user-1");
        vipUsers.add("vip-user-2");
        vipUsers.add("premium-customer");
    }

    public double applyDiscount(String userId, double basePrice) {
        return applyDiscount(userId, basePrice, null, null, null);
    }

    public double applyDiscount(String userId, double basePrice, String promoCode) {
        return applyDiscount(userId, basePrice, promoCode, null, null);
    }

    public double applyDiscount(String userId, double basePrice, String promoCode,
                                LocalDate checkIn, LocalDate checkOut) {
        if (basePrice <= 0) {
            return 0.0;
        }

        double discountedPrice = basePrice;
        List<String> appliedDiscounts = new ArrayList<>();

        // 1. VIP Customer Discount (10% off)
        if (isVipCustomer(userId)) {
            discountedPrice *= 0.90;
            appliedDiscounts.add("VIP-10%");
        }

        // 2. First-time Customer Discount (5% off)
        if (isFirstTimeCustomer(userId)) {
            discountedPrice *= 0.95;
            appliedDiscounts.add("FIRST-TIME-5%");
        }

        // 3. Promo Code Discount
        if (promoCode != null && isValidPromoCode(promoCode)) {
            double promoDiscount = activePromoCodes.get(promoCode);
            discountedPrice *= (1.0 - promoDiscount);
            appliedDiscounts.add("PROMO-" + (int)(promoDiscount * 100) + "%");
        }

        // 4. Long Stay Discount (7+ nights gets 15% off)
        if (checkIn != null && checkOut != null) {
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (nights >= 7) {
                discountedPrice *= 0.85;
                appliedDiscounts.add("LONG-STAY-15%");
            }
        }

        // 5. Weekend Discount (Friday-Sunday gets 8% off)
        if (checkIn != null && isWeekendStay(checkIn)) {
            discountedPrice *= 0.92;
            appliedDiscounts.add("WEEKEND-8%");
        }

        // 6. Maximum discount cap - never more than 60% off
        double maxDiscount = basePrice * 0.60;
        double totalDiscount = basePrice - discountedPrice;
        if (totalDiscount > maxDiscount) {
            discountedPrice = basePrice - maxDiscount;
        }

        // 7. Minimum price floor - never less than $10
        discountedPrice = Math.max(discountedPrice, 10.0);

        return Math.round(discountedPrice * 100.0) / 100.0; // Round to 2 decimal places
    }

    private boolean isVipCustomer(String userId) {
        return vipUsers.contains(userId);
    }

    private boolean isFirstTimeCustomer(String userId) {
        // Simulate checking booking history
        return userId.startsWith("new-") || userId.contains("first");
    }

    private boolean isValidPromoCode(String promoCode) {
        if (!activePromoCodes.containsKey(promoCode)) {
            return false;
        }

        // Simulate expired promo codes
        if ("EXPIRED".equals(promoCode)) {
            return false;
        }

        return true;
    }

    private boolean isWeekendStay(LocalDate checkIn) {
        // Check if check-in is Friday, Saturday, or Sunday
        int dayOfWeek = checkIn.getDayOfWeek().getValue();
        return dayOfWeek >= 5; // Friday=5, Saturday=6, Sunday=7
    }

    // Helper method for testing
    public void addVipUser(String userId) {
        vipUsers.add(userId);
    }

    public void addPromoCode(String code, double discountPercent) {
        activePromoCodes.put(code, discountPercent);
    }
}
