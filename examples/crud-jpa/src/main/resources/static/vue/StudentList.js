import { inject } from 'vue';

export default {
    props: ['data'],
    setup() {
        const { context, message } = inject('VuecanContext', { context: {} });
        return { context, message };
    },
    template: `
        <div class="container animate-fade-in">
            <div v-if="message" class="alert alert-success">{{ message }}</div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                <h2>Students</h2>
                <a href="/students/create" class="btn btn-primary">Create New</a>
            </div>
            <table class="table glass-panel">
                <thead>
                    <tr>
                        <th>First Name</th>
                        <th>Last Name</th>
                        <th>Enrollment Date</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="s in data" :key="s.id">
                        <td>{{ s.firstName }}</td>
                        <td>{{ s.lastName }}</td>
                        <td>{{ s.enrollmentDate }}</td>
                        <td>
                            <a :href="'/students/edit/' + s.id">Edit</a> | 
                            <a :href="'/students/' + s.id">Details</a> | 
                            <a :href="'/students/delete/' + s.id">Delete</a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    `
};
