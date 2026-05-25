# Database Design
![alt text](image.png)

# Exception Handling
Controller
   ↓
Service
   ↓
Exception thrown
   ↓
GlobalExceptionHandler catches it
   ↓
Custom API response returned

@