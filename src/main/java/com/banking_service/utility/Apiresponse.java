package com.banking_service.utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.rmi.registry.LocateRegistry;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Apiresponse {

    private String message;
    private Integer errorCode;
    private LocalDateTime timestamp;
}
