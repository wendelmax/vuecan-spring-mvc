# Vuecan Spring MVC

Vuecan is a powerful Thymeleaf dialect and Spring Boot Starter that bridges the gap between traditional server-side rendered applications and modern Vue.js frontends. It allows you to build declarative Vue 3 components seamlessly inside Spring MVC without the complexity of heavy SPA architectures.

## Key Features

- **Declarative Integration**: Embed Vue directly inside your HTML with `<vue:component>`.
- **Seamless Properties**: Pass Java objects and Spring Model variables to Vue without writing extra REST endpoints.
- **Slots & Children**: Pass server-rendered HTML directly into your Vue components via `<vue:slot>`.
- **Lazy Properties**: Optimize performance by fetching expensive data only when explicitly requested using `LazyProp`.
- **Vite Dev Mode**: First-class support for Vite, enabling lightning-fast Hot Module Replacement (HMR) seamlessly alongside Spring Boot.
- **Automatic TypeScript Generation**: Use `@GenerateTS` to automatically export your Java DTOs to standard `.d.ts` definitions.
- **Partial Updates**: Built-in support for selective HTMX/fetch updates using `X-Partial-Props` headers.
- **Polling**: Easily configure periodic background fetches with `vue:poll`.

## Quick Start

### 1. Install via Maven

Add the Vuecan starter to your Spring Boot project's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.wendelmax</groupId>
    <artifactId>vuecan-spring-mvc-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. Configure Application Properties

Configure the component paths in your `application.properties`:

```properties
# Development Mode (Vite integration)
spring.vuecan.dev-mode=true
spring.vuecan.dev-base=/vue

# Production Configuration
spring.vuecan.prod-base=/assets
spring.vuecan.component-extension=.js
```

### 3. Use in Thymeleaf Templates

Declare the namespace `xmlns:vue="http://vuecan.io"` and mount your component!

```html
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:vue="http://vuecan.io">
<body>
    <vue:context />
    <vue:component vue:name="ProductList"
                   vue:products="${products}"
                   vue:category="${currentCategory}"
                   vue:class="product-grid" />
</body>
</html>
```

The component automatically receives `products` and `category` as standard Vue props, serialized directly from the Spring Model.

## Advanced Usage

### Working with Slots

You can seamlessly pass full HTML content into specific parts of your Vue component using named slots:

```html
<vue:component vue:name="Card">
    <vue:slot vue:name="header">
        <h2>Premium Subscription</h2>
    </vue:slot>
    <p>This will be rendered inside the children slot.</p>
</vue:component>
```

In your Vue component, access the named slot HTML through bounded `slotHeader` props or default `v-html="slotChildren"`.

### Automatic TypeScript Generation

Keep your frontend types perfectly in sync with your backend Java models using the `@GenerateTS` annotation:

```java
import io.github.wendelmax.vuecan.ts.GenerateTS;

@GenerateTS
public class UserDTO {
    private String username;
    private String email;
    private boolean active;
    // getters and setters...
}
```

Vuecan will automatically generate `export interface UserDTO { ... }` in your `vuecan-types.d.ts` file upon application startup!

### Lazy Load Optimization

Avoid expensive database queries on initial render when integrating with partial reloads (e.g., HTMX):

```java
model.addAttribute("lazyStats", LazyProp.optional(() -> analyticsService.computeHeavyStats()));
```

## Examples Showcase

Explore the [examples](./examples) folder to see fully functional Vuecan applications using Vue 3 Composition API:

- **[Store Showcase](./examples/store-showcase)**: A premium, feature-rich store demo with dark mode, glassmorphism, and advanced Vuecan features (Slots, Polling, Context API).
- **[Basics & Hooks](./examples/basics-hooks)**: Simple examples of using Vue 3 Reactive State (ref) and Lifecycle hooks natively.
- **[Form Validation](./examples/form-validation)**: Demonstrating server-side validation feedback interacting with Vue inputs.
- **[CRUD JPA](./examples/crud-jpa)**: A full-stack application leveraging Spring Data JPA, H2, and Vuecan Composition components.

## Testing

To run the full unit and integration test suite:

```bash
mvn -pl vuecan-spring-mvc-starter test
```

A JaCoCo coverage report will be generated at `vuecan-spring-mvc-starter/target/site/jacoco/index.html`.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
