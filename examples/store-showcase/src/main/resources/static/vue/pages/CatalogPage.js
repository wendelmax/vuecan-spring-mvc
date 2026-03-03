import Layout from '../components/Layout.js';
import ProductCard from '../ProductCard.js';

export default {
    props: ['products', 'currentCategory'],
    components: { Layout, ProductCard },
    template: `
        <Layout :title="currentCategory ? 'Category: ' + currentCategory : 'All Products'">
            <div class="product-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem;">
                <template v-if="products && products.length > 0">
                    <ProductCard v-for="p in products" :key="p.id" :product="p" />
                </template>
                <p v-else>No products found.</p>
            </div>
        </Layout>
    `
};
