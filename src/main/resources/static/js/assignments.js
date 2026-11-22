// Assignments: create, submit, grade
// Used by admin.html, lecturer.html and student.html
let asgCourseId = null, asgManage = false, asgList = [], asgSubs = [], asgCurrent = null;

const fmtDateTime = d => d ? new Date(d).toLocaleString("en-GB",
  { day: "numeric", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" }) : "—";

function timeLeft(due) {
  const ms = new Date(due) - new Date();
  if (ms < 0) return "Overdue";
  const d = Math.floor(ms / 86400000), h = Math.floor(ms % 86400000 / 3600000), m = Math.floor(ms % 3600000 / 60000);
  if (d > 0) return `${d} day${d > 1 ? "s" : ""} ${h} hr left`;
  if (h > 0) return `${h} hr ${m} min left`;
  return `${m} min left`;
}

function dueLine(a) {
  const overdue = new Date(a.dueDate) < new Date();
  return `<span class="due ${overdue ? "overdue" : ""}">Due ${fmtDateTime(a.dueDate)} · ${timeLeft(a.dueDate)}</span>`;
}

// Loads assignments into <div id="assignments"></div>
async function renderAssignments(courseId, canManage) {
  asgCourseId = courseId;
  asgManage = canManage;
  asgList = await api(`/api/courses/${courseId}/assignments`);
  $("assignments").innerHTML = `
    <div class="page-head" style="margin-bottom:16px">
      <h2 style="font-size:20px">Assignments</h2>
      ${canManage ? `<button class="btn btn-primary btn-sm" onclick="assignmentForm()">+ New Assignment</button>` : ""}
    </div>
    ${asgList.length
      ? asgList.map(a => canManage ? managerCard(a) : studentCard(a)).join("")
      : `<div class="panel"><div class="empty">No assignments yet.</div></div>`}`;
}

function refreshAssignments() {
  return renderAssignments(asgCourseId, asgManage).catch(e => toast(e.message || "Failed to load assignments", "error"));
}

// ---------- Admin / lecturer view ----------
function managerCard(a) {
  return `
    <div class="assignment">
      <div class="assignment-main">
        <h3>${esc(a.title)}</h3>
        <div class="assignment-meta">${dueLine(a)} · Max ${a.maxMarks} marks</div>
        ${a.instructions ? `<p>${esc(a.instructions)}</p>` : ""}
        <div class="assignment-meta">${a.submissionCount} submitted · ${a.gradedCount} graded</div>
      </div>
      <div class="actions">
        <button class="btn btn-primary btn-sm" onclick="showSubmissions(${a.id})">Submissions (${a.submissionCount})</button>
        <button class="btn btn-light btn-sm" onclick="assignmentForm(${a.id})">Edit</button>
        <button class="btn btn-danger btn-sm" onclick="deleteAssignment(${a.id})">Delete</button>
      </div>
    </div>`;
}

function assignmentForm(id) {
  const a = asgList.find(x => x.id === id) || {};
  const due = a.dueDate ? a.dueDate.slice(0, 16) : "";
  openModal(id ? "Edit Assignment" : "New Assignment", `
      ${field("title", "Title *", { value: a.title, placeholder: "Assignment 1 - OOP Concepts" })}
      ${field("instructions", "Instructions", { value: a.instructions, textarea: true, placeholder: "What students must do..." })}
      <div class="grid2">
        ${field("dueDate", "Due date *", { type: "datetime-local", value: due })}
        ${field("maxMarks", "Max marks *", { type: "number", value: a.maxMarks ?? 100 })}
      </div>`,
    async () => {
      if (!val("dueDate")) throw { errors: { dueDate: "Due date is required" } };
      const body = JSON.stringify({
        title: val("title"), instructions: val("instructions"),
        dueDate: val("dueDate"), maxMarks: Number(val("maxMarks"))
      });
      if (id) await api(`/api/assignments/${id}`, { method: "PUT", body });
      else await api(`/api/courses/${asgCourseId}/assignments`, { method: "POST", body });
      toast(id ? "Assignment updated" : "Assignment created");
      refreshAssignments();
    },
    id ? "Save changes" : "Create");
}

async function deleteAssignment(id) {
  if (!confirm("Delete this assignment and ALL student submissions?")) return;
  try {
    await api(`/api/assignments/${id}`, { method: "DELETE" });
    toast("Assignment deleted");
    refreshAssignments();
  } catch (e) { toast(e.message || "Delete failed", "error"); }
}

async function showSubmissions(id) {
  asgCurrent = asgList.find(a => a.id === id);
  asgSubs = await api(`/api/assignments/${id}/submissions`);
  const a = asgCurrent;
  const graded = asgSubs.filter(s => s.marks != null).length;

  $("assignments").innerHTML = `
    <a class="back" onclick="refreshAssignments()">← All assignments</a>
    <div class="panel">
      <div class="panel-head">
        <div><h2>${esc(a.title)}</h2><div class="assignment-meta">${dueLine(a)} · Max ${a.maxMarks} marks</div></div>
        <span class="muted">${asgSubs.length} submitted · ${graded} graded</span>
      </div>
      ${asgSubs.length ? `
        <table>
          <thead><tr><th>Student</th><th class="hide-sm">Submitted</th><th>Status</th><th>File</th><th>Marks</th><th></th></tr></thead>
          <tbody>${asgSubs.map(s => `
            <tr>
              <td>
                <div class="person"><div class="avatar">${esc(initials(s.studentName))}</div>
                  <div>${esc(s.studentName)}<small>${esc(s.studentEmail)}</small></div></div>
                ${s.comment ? `<div class="sub-comment">"${esc(s.comment)}"</div>` : ""}
              </td>
              <td class="hide-sm">${fmtDateTime(s.submittedAt)}</td>
              <td><span class="status ${s.late ? "status-late" : "status-ok"}">${s.late ? "Late" : "On time"}</span></td>
              <td><button class="btn btn-light btn-sm" onclick="downloadSubmission(${s.id})">Download</button></td>
              <td>${s.marks != null ? `<b>${s.marks}</b> / ${a.maxMarks}` : `<span class="muted">Not graded</span>`}</td>
              <td style="text-align:right">
                <button class="btn btn-primary btn-sm" onclick="gradeForm(${s.id})">${s.marks != null ? "Edit grade" : "Grade"}</button>
              </td>
            </tr>`).join("")}
          </tbody>
        </table>`
      : `<div class="empty">No submissions yet.</div>`}
    </div>`;
}

function gradeForm(id) {
  const s = asgSubs.find(x => x.id === id);
  openModal(`Grade: ${esc(s.studentName)}`, `
      ${field("marks", `Marks * (out of ${asgCurrent.maxMarks})`, { type: "number", value: s.marks ?? "" })}
      ${field("feedback", "Feedback", { value: s.feedback, textarea: true, placeholder: "Good work..." })}`,
    async () => {
      if (val("marks") === "") throw { errors: { marks: "Marks are required" } };
      await api(`/api/submissions/${id}/grade`, {
        method: "PUT",
        body: JSON.stringify({ marks: Number(val("marks")), feedback: val("feedback") })
      });
      toast("Grade saved");
      showSubmissions(asgCurrent.id);
    },
    "Save grade");
}

// ---------- Student view ----------
function studentCard(a) {
  const s = a.mySubmission;
  const graded = s && s.marks != null;
  let status;
  if (!s) status = `<span class="status status-none">Not submitted</span>`;
  else if (graded) status = `<span class="status status-graded">Graded: ${s.marks} / ${a.maxMarks}</span>`;
  else status = `<span class="status ${s.late ? "status-late" : "status-ok"}">Submitted${s.late ? " late" : ""}</span>`;

  return `
    <div class="assignment">
      <div class="assignment-main">
        <h3>${esc(a.title)} ${status}</h3>
        <div class="assignment-meta">${dueLine(a)} · Max ${a.maxMarks} marks</div>
        ${a.instructions ? `<p>${esc(a.instructions)}</p>` : ""}
        ${s ? `
          <div class="my-submission">
            Your file: <button class="link-btn" onclick="downloadSubmission(${s.id})">${esc(s.fileName)}</button>
            · submitted ${fmtDateTime(s.submittedAt)}${s.late ? ' <b class="text-late">(late)</b>' : ""}
            ${graded && s.feedback ? `<div class="feedback"><b>Feedback:</b> ${esc(s.feedback)}</div>` : ""}
          </div>` : ""}
      </div>
      <div class="actions">
        ${graded ? "" : `<button class="btn btn-primary btn-sm" onclick="submitForm(${a.id})">${s ? "Re-submit" : "Submit"}</button>`}
      </div>
    </div>`;
}

function submitForm(id) {
  const a = asgList.find(x => x.id === id);
  const late = new Date(a.dueDate) < new Date();
  openModal(`Submit: ${esc(a.title)}`, `
      ${late ? `<div class="alert">The deadline has passed. Your submission will be marked <b>LATE</b>.</div>` : ""}
      ${a.mySubmission ? `<p class="muted" style="margin-bottom:14px">Uploading a new file will replace your previous submission.</p>` : ""}
      <div class="field">
        <label>Your file * (max ${MAX_FILE_MB}MB)</label>
        <input id="file" type="file">
        <div class="err" id="fileErr"></div>
      </div>
      ${field("comment", "Comment (optional)", { textarea: true, placeholder: "Any note for your lecturer..." })}`,
    async () => {
      const file = $("file").files[0];
      if (!file) throw { errors: { file: "Please choose a file" } };
      if (file.size > MAX_FILE_MB * 1024 * 1024) throw { errors: { file: `File is too large (max ${MAX_FILE_MB}MB)` } };
      const form = new FormData();
      form.append("file", file);
      form.append("comment", val("comment"));
      await api(`/api/assignments/${id}/submit`, { method: "POST", body: form });
      toast("Assignment submitted");
      refreshAssignments();
    },
    "Submit");
}

// ---------- Download (student's own file, or admin/lecturer) ----------
async function downloadSubmission(id) {
  const s = asgSubs.find(x => x.id === id) || asgList.map(a => a.mySubmission).find(x => x && x.id === id);
  try {
    await downloadFile(`/api/submissions/${id}/download`, s ? s.fileName : "submission");
  } catch (e) { toast(e.message || "Download failed", "error"); }
}
