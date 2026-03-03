export default {
    props: ['isOpen', 'items'],
    emits: ['close', 'remove'],
    template: `
        <div :class="['cart-drawer', isOpen ? 'open' : '']">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
                <h2>Your Cart</h2>
                <button @click="$emit('close')" style="background: transparent; border: none; color: white; font-size: 1.5rem; cursor: pointer;">×</button>
            </div>
            <div style="overflow-y: auto; max-height: calc(100vh - 200px);">
                <p v-if="!items || items.length === 0" style="color: var(--text-muted);">Cart is empty</p>
                <div v-else v-for="(item, idx) in items" :key="item.id + '-' + idx" class="cart-item" style="display:flex; justify-content: space-between; margin-bottom: 1rem;">
                    <div>
                        <p style="font-weight: bold; margin: 0;">{{ item.name }}</p>
                        <p style="font-size: 0.8rem; color: var(--text-muted); margin: 0;">{{ item.category }}</p>
                    </div>
                    <button @click="$emit('remove', idx)" style="background: transparent; border: none; color: #ff4444; cursor: pointer;">Remove</button>
                </div>
            </div>
            <div v-if="items && items.length > 0" style="margin-top: 2rem;">
                <button class="btn btn-primary" style="width: 100%;">Checkout Now</button>
            </div>
        </div>
    `
};
