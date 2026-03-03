export default {
    props: ['data'],
    template: `
        <div v-if="!data">Student not found.</div>
        <div v-else class="container glass-panel animate-fade-in">
            <h2>Student Details</h2>
            <dl class="row">
                <dt class="col-sm-2">First Name</dt>
                <dd class="col-sm-10">{{ data.firstName }}</dd>
                <dt class="col-sm-2">Last Name</dt>
                <dd class="col-sm-10">{{ data.lastName }}</dd>
                <dt class="col-sm-2">Enrollment Date</dt>
                <dd class="col-sm-10">{{ data.enrollmentDate }}</dd>
            </dl>
            <div>
                <a :href="'/students/edit/' + data.id" class="btn btn-primary">Edit</a>
                <a href="/students" class="btn btn-secondary">Back to List</a>
            </div>
        </div>
    `
};
