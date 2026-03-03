export default {
    props: ['product', 'onAddToCart', 'slotHeader', 'slotChildren'],
    template: `
        <div class="product-card glass-panel animate-fade-in">
            <div v-if="slotHeader" class="slot-header" style="padding: 0.5rem; background: rgba(99, 102, 241, 0.2); font-size: 0.75rem; text-align: center;" v-html="slotHeader"></div>
            <div style="height: 140px; background: linear-gradient(45deg, #1e293b, #334155); display: flex; align-items: center; justify-content: center; font-size: 2.5rem;">
                📦
            </div>
            <div class="card-content">
                <span class="product-category">{{ product.category }}</span>
                <h3 class="product-title">{{ product.name }}</h3>
                <div v-if="slotChildren" class="slot-children" style="margin: 0.5rem 0; font-size: 0.875rem; color: var(--text-muted);" v-html="slotChildren"></div>
                <div style="margin-top: auto; display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-size: 1.25rem; font-weight: bold; color: var(--accent);">$99.99</span>
                    <button class="btn btn-primary" @click="onAddToCart ? onAddToCart(product) : alert('Added!')">Add to Cart</button>
                </div>
            </div>
        </div>
    `
};
