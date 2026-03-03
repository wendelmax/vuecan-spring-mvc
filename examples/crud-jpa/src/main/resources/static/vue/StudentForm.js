import { ref, inject } from 'vue';

export default {
    props: ['data', 'isEdit'],
    setup(props) {
        const vuecan = inject('VuecanContext', {});
        const serverErrors = vuecan.errors || [];
        const message = vuecan.message;

        const student = ref(props.data || { firstName: '', lastName: '', enrollmentDate: '' });

        const getError = (field) => serverErrors.find(e => e.field === field)?.message;

        return { student, message, getError, isEdit: props.isEdit };
    },
    template: `
        <div class="container glass-panel animate-fade-in">
            <h2>{{ isEdit ? 'Edit Student' : 'Create Student' }}</h2>
            <div v-if="message" class="alert alert-info">{{ message }}</div>
            <form method="POST" :action="isEdit ? '/students/edit/' + student.id : '/students/create'">
                <div class="form-group">
                    <label>First Name</label>
                    <input name="firstName" class="form-control" v-model="student.firstName" />
                    <span v-if="getError('firstName')" class="text-danger">{{ getError('firstName') }}</span>
                </div>
                <div class="form-group">
                    <label>Last Name</label>
                    <input name="lastName" class="form-control" v-model="student.lastName" />
                    <span v-if="getError('lastName')" class="text-danger">{{ getError('lastName') }}</span>
                </div>
                <div class="form-group">
                    <label>Enrollment Date</label>
                    <input type="date" name="enrollmentDate" class="form-control" v-model="student.enrollmentDate" />
                    <span v-if="getError('enrollmentDate')" class="text-danger">{{ getError('enrollmentDate') }}</span>
                </div>
                <div style="margin-top: 1rem;">
                    <button type="submit" class="btn btn-primary">Save</button>
                    <a href="/students" class="btn btn-secondary">Back to List</a>
                </div>
            </form>
        </div>
    `
};
