export default {
    props: ['categories', 'currentCategory'],
    emits: ['search'],
    template: `
        <aside class="sidebar glass-panel">
            <div style="margin-bottom: 2rem;">
                <h4 style="margin-bottom: 1rem; color: var(--secondary);">Search</h4>
                <input type="text" placeholder="Looking for something?" @input="$emit('search', $event.target.value)" style="width: 100%; padding: 0.75rem; border-radius: 8px; border: 1px solid var(--glass-border); background: rgba(255,255,255,0.05); color: white; outline: none;" />
            </div>
            <div>
                <h4 style="margin-bottom: 1rem; color: var(--secondary);">Categories</h4>
                <ul style="list-style: none; padding: 0;">
                    <li style="margin-bottom: 0.5rem;">
                        <a href="/products" :style="{ color: !currentCategory ? 'var(--accent)' : 'var(--text-muted)', textDecoration: 'none' }">All Categories</a>
                    </li>
                    <li v-for="cat in categories" :key="cat" style="margin-bottom: 0.5rem;">
                        <a :href="'/products?category=' + cat" :style="{ color: currentCategory === cat ? 'var(--accent)' : 'var(--text-muted)', textDecoration: 'none' }">{{ cat.charAt(0).toUpperCase() + cat.slice(1) }}</a>
                    </li>
                </ul>
            </div>
        </aside>
    `
};
