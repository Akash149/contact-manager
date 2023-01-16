package com.smartcontactmanager.helper;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class DbHealth implements HealthIndicator {

    public static final String DB_SERVICE = "DbHelth";

    public boolean isHealthGood() {
        // TODO custom logic

        return true;
    }

    @Override
    public Health health() {
        // TODO Auto-generated method stub
        if(isHealthGood()) {
            return Health.up().withDetail(DB_SERVICE, "Database service is running up !!").build();
        }
        return Health.down().withDetail(DB_SERVICE, "Database service is running down !!").build();
    }
    
}
