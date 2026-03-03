# Vuecan Basics - Hooks & State

This example demonstrates how to use standard Vue Hooks (`useState` and `useEffect`) within a Vuecan component.

## Key Features
- **Props Integration**: The `initialValue` is passed from a Spring MVC Controller via Thymeleaf.
- **State Management**: Using `useState` to track the counter and the last action performed.
- **Side Effects**: Using `useEffect` to log changes to the count.

## How it works
1. The **Spring Controller** adds `initialCount` to the model.
2. The **Thymeleaf Template** uses `<vue:component>` to render the `Counter` component, passing the value from the model.
3. The **Vue Component** initializes its state using the prop and manages user interactions locally.

## Running the example
```bash
mvn spring-boot:run
```
Access at `http://localhost:8080`
