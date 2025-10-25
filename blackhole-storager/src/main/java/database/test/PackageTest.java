package database.test;

import database.BlackholeStorageManager;
import database.config.ConfigurationManager;
import database.core.DatabaseStorage;
import database.core.StorageException;

/**
 * Package structure test
 */
public class PackageTest {
    
    public static void main(String[] args) {
        System.out.println("=== Package Structure Test ===");
        
        try {
            // Test imports
            BlackholeStorageManager manager = BlackholeStorageManager.getInstance();
            System.out.println("SUCCESS: BlackholeStorageManager imported");
            
            ConfigurationManager configManager = new ConfigurationManager();
            System.out.println("SUCCESS: ConfigurationManager imported");
            
            System.out.println("=== All package imports test passed ===");
            
        } catch (Exception e) {
            System.err.println("Package import test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}