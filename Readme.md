# Database Design
![alt text](image.png)

# Exception Handling
```mermaid
graph TD
    Controller --> Service
    Service -->|Exception Thrown| GlobalExceptionHandler[GlobalExceptionHandler Catches It]
    GlobalExceptionHandler -->|Returns| CustomApiResponse[Custom API Response]
```

