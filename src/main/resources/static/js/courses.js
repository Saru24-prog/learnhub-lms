// Course management screens - used by BOTH admin.html and lecturer.html
// (Admin can manage every course, a lecturer only their own - the backend checks this)
let myCourses = [];

// ---------- Courses (cards) ----------
async function renderCourses() {
  myCourses = await api("/api/lecturer/courses");
  $("main").innerHTML = `
    <div class="page-head">
      <div class="page-title"><h1>Courses</h1><p>Create courses and share the enrollment key with your students</p></div>
      <button class="btn btn-primary" onclick="courseForm()">+ New Course</button>
    </div>
    ${myCourses.length
      ? `<div class="courses">${myCourses.map(courseCard).join("")}</div>`
      : `<div class="panel"><div class="empty">No courses yet. Click <b>+ New Course</b> to create your first one.</div></div>`}`;
}

function courseCard(c) {
  return `
    <div class="course">
      <div class="course-top" style="background-color:${courseColor(c.id)}">${esc(c.code)}</div>
      <div class="course-body">
        <h3>${esc(c.title)}</h3>
        <p>${esc(c.description) || "No description"}</p>
        <div class="course-meta">
          <span>${c.studentCount} student${c.studentCount === 1 ? "" : "s"}</span>
          <span>Key: <span class="key">${esc(c.enrollmentKey)}</span></span>
        </div>
      </div>
      <div class="course-actions">
        <button class="btn btn-primary btn-sm" onclick="go('course', ${c.id})">Open</button>
        <button class="btn btn-light btn-sm" onclick="courseForm(${c.id})">Edit</button>
        <button class="btn btn-danger btn-sm" onclick="deleteCourse(${c.id})">Delete</button>
      </div>
    </div>`;
}

// ---------- Course detail: enrolled students ----------
async function renderCourseDetail(id) {
  myCourses = await api("/api/lecturer/courses");
  const c = myCourses.find(x => x.id == id);
  if (!c) { toast("Course not found", "error"); go("courses"); return; }
  const students = await api(`/api/lecturer/courses/${id}/students`);

  $("main").innerHTML = `
    <a class="back" onclick="go('courses')">← Back to Courses</a>
    <div class="course-hero" style="background-color:${courseColor(c.id)}">
      <small>${esc(c.code)} · ${esc(c.lecturerName)}</small>
      <h1>${esc(c.title)}</h1>
      <p>${esc(c.description)}</p>
    </div>
    ${statCards([
      ["Enrolled Students", students.length],
      ["Enrollment Key", `<span class="key">${esc(c.enrollmentKey)}</span>`],
      ["Created", fmtDate(c.createdAt)]
    ])}
    <div id="content"></div>
    <div id="assignments"></div>
    <div class="panel">
      <div class="panel-head">
        <h2>Enrolled Students (${students.length})</h2>
        <button class="btn btn-light btn-sm" onclick="courseForm(${c.id})">Edit course</button>
      </div>
      ${students.length ? `
        <table>
          <thead><tr><th>Student</th><th class="hide-sm">Enrolled on</th><th></th></tr></thead>
          <tbody>${students.map(s => `
            <tr>
              <td><div class="person"><div class="avatar">${esc(initials(s.fullName))}</div>
                <div>${esc(s.fullName)}<small>${esc(s.email)}</small></div></div></td>
              <td class="hide-sm">${fmtDate(s.enrolledAt)}</td>
              <td style="text-align:right">
                <button class="btn btn-danger btn-sm" onclick="removeStudent(${c.id}, ${s.id})">Remove</button>
              </td>
            </tr>`).join("")}
          </tbody>
        </table>`
      : `<div class="empty">No students yet. Share the enrollment key <span class="key">${esc(c.enrollmentKey)}</span> with your class.</div>`}
    </div>`;
  const data = await renderContent(id);
  await renderAssignments(id, data.canManage);
}

// ---------- Create / Edit course popup ----------
function courseForm(id) {
  const c = myCourses.find(x => x.id === id) || {};
  openModal(id ? "Edit Course" : "Create New Course", `
      <div class="grid2">
        ${field("code", "Course code *", { value: c.code, placeholder: "IT2030" })}
        ${field("enrollmentKey", "Enrollment key *", { value: c.enrollmentKey, placeholder: "oop2026" })}
      </div>
      ${field("title", "Course title *", { value: c.title, placeholder: "Object Oriented Programming" })}
      ${field("description", "Description", { value: c.description, textarea: true, placeholder: "What students will learn..." })}`,
    async () => {
      const body = { code: val("code"), title: val("title"), description: val("description"), enrollmentKey: val("enrollmentKey") };
      await api(id ? `/api/lecturer/courses/${id}` : "/api/lecturer/courses", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify(body)
      });
      toast(id ? "Course updated" : "Course created");
      route();
    },
    id ? "Save changes" : "Create course");
}

async function deleteCourse(id) {
  if (!confirm("Delete this course? All student enrollments will also be removed.")) return;
  try {
    await api(`/api/lecturer/courses/${id}`, { method: "DELETE" });
    toast("Course deleted");
    go("courses");
  } catch (e) { toast(e.message || "Delete failed", "error"); }
}

async function removeStudent(courseId, studentId) {
  if (!confirm("Remove this student from the course?")) return;
  try {
    await api(`/api/lecturer/courses/${courseId}/students/${studentId}`, { method: "DELETE" });
    toast("Student removed");
    route();
  } catch (e) { toast(e.message || "Failed", "error"); }
}
