import { ref, onMounted, onUnmounted } from 'vue';

export default {
    props: ['preloadUrl', 'refetchInterval'],
    setup(props) {
        const data = ref(null);
        const loading = ref(true);

        const fetchData = async () => {
            try {
                const response = await fetch(props.preloadUrl || '/products/api/status');
                const result = await response.json();
                if (result.success) {
                    data.value = result.data;
                }
            } catch (error) {
                console.error('Status fetch failed', error);
            } finally {
                loading.value = false;
            }
        };

        let timer;
        onMounted(() => {
            fetchData();
            if (props.refetchInterval) {
                timer = setInterval(fetchData, props.refetchInterval);
            }
        });

        onUnmounted(() => {
            if (timer) clearInterval(timer);
        });

        return { data, loading };
    },
    template: `
        <div class="glass-panel" style="padding: 1rem; margin-bottom: 1rem; border-left: 4px solid var(--accent);">
            <h4 style="margin-bottom: 0.5rem; display: flex; align-items: center; gap: 0.5rem;">
                <span class="pulse" style="width: 10px; height: 10px; background: #4ade80; border-radius: 50%;"></span>
                Live System Status
            </h4>
            <p v-if="loading">Loading...</p>
            <div v-else-if="data" style="font-size: 0.875rem;">
                <p>Status: {{ data.status }}</p>
                <p>Active Users: {{ data.activeUsers }}</p>
                <p style="color: var(--text-muted); font-size: 0.75rem;">Server Time: {{ data.serverTime }}</p>
            </div>
        </div>
    `
};
