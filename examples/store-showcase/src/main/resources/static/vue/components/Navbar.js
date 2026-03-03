export default {
    props: ['cartCount'],
    emits: ['cartClick'],
    template: `
        <nav class="navbar glass-panel">
            <div class="logo" style="font-size: 1.5rem; font-weight: bold; display: flex; align-items: center; gap: 0.5rem;">
                <span style="color: var(--primary);">⚡</span>
                Vuecan Store
            </div>
            <div class="nav-actions" style="display: flex; gap: 1.5rem; align-items: center;">
                <button class="btn" style="background: transparent; color: var(--text-main); position: relative;" @click="$emit('cartClick')">
                    🛒 Cart
                    <span v-if="cartCount > 0" class="cart-badge" style="position: absolute; top: -5px; right: -5px;">{{ cartCount }}</span>
                </button>
            </div>
        </nav>
    `
};
