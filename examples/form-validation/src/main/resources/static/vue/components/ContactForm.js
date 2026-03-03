import { reactive } from 'vue';

export default {
    setup() {
        const formData = reactive({ name: '', email: '', message: '' });
        const status = reactive({ type: 'idle', message: '', errors: {} });

        const handleSubmit = async () => {
            status.type = 'loading';
            status.message = 'Sending...';
            status.errors = {};

            try {
                const response = await fetch('/api/contact', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(formData)
                });

                const result = await response.json();

                if (result.success) {
                    status.type = 'success';
                    status.message = result.data;
                    status.errors = {};
                    formData.name = '';
                    formData.email = '';
                    formData.message = '';
                } else {
                    const fieldErrors = {};
                    if (result.errors) {
                        result.errors.forEach(err => {
                            fieldErrors[err.field] = err.message;
                        });
                    }
                    status.type = 'error';
                    status.message = result.message || 'Check the fields below';
                    status.errors = fieldErrors;
                }
            } catch (err) {
                status.type = 'error';
                status.message = 'Connection failed';
                status.errors = {};
            }
        };

        const inputStyle = (field) => ({
            width: '100%',
            padding: '10px',
            border: `1px solid ${status.errors[field] ? '#dc3545' : '#ccc'}`,
            borderRadius: '4px',
            marginBottom: '5px'
        });

        return { formData, status, handleSubmit, inputStyle };
    },
    template: `
        <form @submit.prevent="handleSubmit" style="display: flex; flex-direction: column; gap: 15px;">
            <div>
                <label style="display: block; margin-bottom: 5px;">Name</label>
                <input name="name" v-model="formData.name" :style="inputStyle('name')" />
                <span v-if="status.errors.name" style="color: #dc3545; font-size: 0.8rem;">{{ status.errors.name }}</span>
            </div>
            <div>
                <label style="display: block; margin-bottom: 5px;">Email</label>
                <input name="email" type="email" v-model="formData.email" :style="inputStyle('email')" />
                <span v-if="status.errors.email" style="color: #dc3545; font-size: 0.8rem;">{{ status.errors.email }}</span>
            </div>
            <div>
                <label style="display: block; margin-bottom: 5px;">Message</label>
                <textarea name="message" v-model="formData.message" :style="{ ...inputStyle('message'), height: '100px' }"></textarea>
                <span v-if="status.errors.message" style="color: #dc3545; font-size: 0.8rem;">{{ status.errors.message }}</span>
            </div>
            <button :disabled="status.type === 'loading'" type="submit" :style="{
                padding: '12px',
                background: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: status.type === 'loading' ? 'not-allowed' : 'pointer'
            }">{{ status.type === 'loading' ? 'Submitting...' : 'Send Message' }}</button>
            
            <div v-if="status.message" :style="{
                padding: '10px',
                borderRadius: '4px',
                background: status.type === 'success' ? '#d4edda' : '#f8d7da',
                color: status.type === 'success' ? '#155724' : '#721c24'
            }">{{ status.message }}</div>
        </form>
    `
};
