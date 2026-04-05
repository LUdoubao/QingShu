package org.doubao.interview.agent.server;

import org.doubao.interview.agent.server.collection.ArrayListPrincipleDemo;
import org.doubao.interview.agent.server.collection.CollectionMapComparisonDemo;
import org.doubao.interview.agent.server.collection.CollectionSetComparisonDemo;
import org.doubao.interview.agent.server.collection.HashSetHashMapRelationshipDemo;
import org.doubao.interview.agent.server.collection.SimpleArrayListTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InterviewAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewAgentApplication.class, args);
        
        // Run the HashSet/HashMap relationship demo after application startup
        System.out.println("\n--- Running HashSet and HashMap Relationship Demo ---");
        HashSetHashMapRelationshipDemo.main(args);
        
        // Run the Set comparison demo after application startup
        System.out.println("\n--- Running HashSet, LinkedHashSet, TreeSet Comparison Demo ---");
        CollectionSetComparisonDemo.main(args);
        
        // Run the Map comparison demo after application startup
        System.out.println("\n--- Running HashMap, LinkedHashMap, TreeMap Comparison Demo ---");
        CollectionMapComparisonDemo.main(args);
        
        // Run the ArrayList principle demo after application startup
        System.out.println("\n--- Running ArrayList Principle Demo ---");
        ArrayListPrincipleDemo.main(args);
        
        // Run the SimpleArrayList test after application startup
        System.out.println("\n--- Running SimpleArrayList Test ---");
        SimpleArrayListTest.main(args);
    }
}