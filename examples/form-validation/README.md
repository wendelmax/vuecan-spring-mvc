# Vuecan - Form Validation

This example demonstrates how to handle forms and server-side validation using **VuecanResponse**.

## Key Features
- **Server-Side Validation**: Using standard `jakarta.validation` annotations in Java.
- **Automated Mapping**: Vuecan's `VuecanResponse` structure makes it easy for the Vue component to map error messages back to specific fields.
- **Loading States**: Managing UI states (idle, loading, success, error) during the request lifecycle.

## How it works
1. The **Vue Component** captures user input using `useState`.
2. On submit, it sends a JSON payload to the **Spring Controller**.
3. If valid, the controller returns `VuecanResponse.ok()`.
4. If invalid, Spring's `@Valid` catches errors, and they are returned in the `errors` array of the `VuecanResponse`.
5. The component iterates over these errors to display them under the correct inputs.

## Running the example
```bash
mvn spring-boot:run
```
Access at `http://localhost:8080`
