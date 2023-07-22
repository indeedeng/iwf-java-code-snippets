package io.workflow.workflow.admincontrolledfailback;

import org.springframework.stereotype.Service;

@Service
public class AdminControlledFailbackDependencyService {

    private int mockExceptionCount = 0;

    public void simpleMethod() {
        if (mockExceptionCount++ < 3) {
            throw new RuntimeException("Exception: " + mockExceptionCount);
        }
        mockExceptionCount = 0;
        System.out.println("DependencyService.simpleMethod done!");
    }
}
