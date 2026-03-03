export default {
    props: ['data'],
    template: `
        <div v-if="!data">Student not found.</div>
        <div v-else class="container glass-panel animate-fade-in">
            <h2 class="text-danger">Delete Student</h2>
            <p>Are you sure you want to delete {{ data.firstName }} {{ data.lastName }}?</p>
            <form method="POST" :action="'/students/delete/' + data.id">
                <button type="submit" class="btn btn-danger">Delete</button>
                <a href="/students" class="btn btn-secondary">Cancel</a>
            </form>
        </div>
    `
};
