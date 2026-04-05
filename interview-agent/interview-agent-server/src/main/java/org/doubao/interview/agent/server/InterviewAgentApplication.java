package org.doubao.interview.agent.server;

import org.doubao.interview.agent.server.collection.HashSetHashMapRelationshipDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InterviewAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewAgentApplication.class, args);
        
        // Run the HashSet/HashMap relationship demo after application startup
        System.out.println("\n--- Running HashSet and HashMap Relationship Demo ---");
        HashSetHashMapRelationshipDemo.main(args);
    }
}