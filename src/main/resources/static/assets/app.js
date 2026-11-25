/* TicketHub Lab 09 UI
   IMPORTANT: Update DEMO_ACCOUNTS with the exact demo usernames/passwords supplied by your lecturer.
   This UI expects the Lab 09 REST endpoints described in the lab sheet.
*/
const DEMO_ACCOUNTS = {
  ADMIN:     { username: "admin", password: "CHANGE_ME" },
  ORGANIZER: { username: "organizer", password: "CHANGE_ME" },
  CUSTOMER:  { username: "customer", password: "CHANGE_ME" }
};

const state = { token: localStorage.getItem("tickethub_token") || "", user: null, events: [], reservations: [], users: [], editingEventId: null };
const $ = (id) => document.getElementById(id);
const esc = (v) => String(v ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
const role = () => String(state.user?.role || state.user?.authorities?.[0]?.authority || "").replace("ROLE_","").toUpperCase();
const isAdmin = () => role() === "ADMIN";
const isOrganizer = () => role() === "ORGANIZER";
const isCustomer = () => role() === "CUSTOMER";
const money = (v) => Number(v || 0).toLocaleString(undefined,{minimumFractionDigits:2,maximumFractionDigits:2});
const dateText = (v) => { if(!v) return "Date not set"; const d=new Date(v); return isNaN(d) ? esc(v) : d.toLocaleString(); };

async function api(path, {method="GET", body, token=state.token} = {}) {
  const headers = {"Content-Type":"application/json"};
  if(token) headers.Authorization = "Bearer " + token;
  const res = await fetch(path,{method,headers,body:body === undefined ? undefined : JSON.stringify(body)});
  const raw = await res.text(); let data = null;
  try { data = raw ? JSON.parse(raw) : null; } catch { data = raw; }
  if(!res.ok) {
    const message = data?.message || data?.error || (typeof data === "string" ? data : `HTTP ${res.status}`);
    if(res.status === 401 && token) logout("Your session expired. Please sign in again.");
    const err = new Error(message); err.status = res.status; throw err;
  }
  return data;
}
function notify(message, type="info") {
  const el=$("global-message"); el.textContent=message; el.classList.remove("hidden");
  el.style.background = type==="error" ? "#a52d40" : type==="success" ? "#126b4b" : "#172033";
  clearTimeout(notify.timer); notify.timer=setTimeout(()=>el.classList.add("hidden"),5000);
}
function authMessage(message, ok=false) { const el=$("auth-message"); el.textContent=message; el.classList.remove("hidden"); el.classList.toggle("ok",ok); }
function showAuth(tab="login") {
  $("auth-view").classList.remove("hidden"); $("app-view").classList.add("hidden");
  $("logout-btn").classList.add("hidden"); $("session-label").textContent="Not signed in";
  document.querySelectorAll("[data-auth-tab]").forEach(b=>b.classList.toggle("active",b.dataset.authTab===tab));
  $("login-form").classList.toggle("hidden",tab!=="login"); $("register-form").classList.toggle("hidden",tab!=="register");
}
function showApp() {
  $("auth-view").classList.add("hidden"); $("app-view").classList.remove("hidden"); $("logout-btn").classList.remove("hidden");
  $("session-label").textContent=state.user?.username || state.user?.fullName || "Signed in";
  $("welcome-title").textContent=`Welcome, ${state.user?.fullName || state.user?.username || "User"}`;
  $("welcome-subtitle").textContent="Manage your events and reservations.";
  $("role-pill").textContent=role() || "USER";
  document.querySelectorAll(".staff-only").forEach(x=>x.classList.toggle("hidden",!(isAdmin()||isOrganizer())));
  document.querySelectorAll(".admin-only").forEach(x=>x.classList.toggle("hidden",!isAdmin()));
  loadEvents();
}
function logout(message) {
  state.token=""; state.user=null; localStorage.removeItem("tickethub_token"); showAuth();
  if(message) notify(message,"error");
}
async function login(username,password) {
  const data=await api("/api/auth/login",{method:"POST",body:{username,password},token:""});
  const token=data?.token || data?.accessToken || data?.jwt;
  if(!token) throw new Error("Login response did not contain token/accessToken. Check AuthResponse field names.");
  state.token=token; localStorage.setItem("tickethub_token",token);
  try { state.user=await api("/api/auth/me"); } catch { state.user=data.user || data; }
  showApp(); notify("Signed in successfully.","success");
}
function activatePanel(id) {
  document.querySelectorAll(".panel").forEach(p=>p.classList.toggle("hidden",p.id!==id));
  document.querySelectorAll(".nav-tab").forEach(b=>b.classList.toggle("active",b.dataset.panel===id));
  if(id==="reservations-panel") loadReservations();
  if(id==="users-panel") loadUsers();
  if(id==="stats-panel") loadStats();
}
async function loadEvents() {
  try {
    const q=$("event-search").value.trim();
    const data=await api("/api/events"+(q?"?keyword="+encodeURIComponent(q):""));
    state.events=Array.isArray(data)?data:(data?.content||data?.events||[]);
    renderEvents();
  } catch(e) { notify(`Could not load events: ${e.message}`,"error"); }
}
function renderEvents() {
  const list=$("events-list"); list.innerHTML="";
  $("events-empty").classList.toggle("hidden",state.events.length>0);
  state.events.forEach(ev=>{
    const id=ev.id||ev.eventId; const total=Number(ev.totalSeats||0), available=Number(ev.availableSeats ?? total);
    const sold=Math.max(0,total-available), pct=total?Math.min(100,sold/total*100):0;
    const canManage=isAdmin() || (isOrganizer() && (ev.organizer?.username===state.user?.username || ev.organizerUsername===state.user?.username));
    const card=document.createElement("article"); card.className="event-card";
    card.innerHTML=`<div><h3>${esc(ev.name)}</h3><p>${esc(ev.description||"Event experience")}</p></div>
      <div class="event-meta"><div>Venue<strong>${esc(ev.venue)}</strong></div><div>Date<strong>${esc(dateText(ev.date||ev.eventDate))}</strong></div>
      <div>Ticket price<strong>${money(ev.ticketPrice)}</strong></div><div>Seats available<strong>${available} / ${total}</strong></div></div>
      <div><div class="seat-track"><span style="width:${pct}%"></span></div><p class="tiny">${sold} seats reserved · ${available} remaining</p></div>
      <div class="card-actions">
      ${isCustomer()?`<button class="btn btn-primary" data-book="${esc(id)}">Book tickets</button>`:""}
      ${canManage?`<button class="btn btn-soft" data-edit-event="${esc(id)}">Edit</button>`:""}
      ${isAdmin()?`<button class="btn btn-ghost" data-delete-event="${esc(id)}">Delete</button>`:""}
      </div>`;
    list.appendChild(card);
  });
}
function openEventForm(ev=null) {
  state.editingEventId=ev?(ev.id||ev.eventId):null;
  $("event-form-title").textContent=ev?"Edit event":"Create event";
  $("event-id").value=state.editingEventId||"";
  $("event-name").value=ev?.name||""; $("event-venue").value=ev?.venue||"";
  $("event-description").value=ev?.description||"";
  const dt=ev?.date||ev?.eventDate; $("event-date").value=dt?String(dt).slice(0,16):"";
  $("event-total-seats").value=ev?.totalSeats??20; $("event-price").value=ev?.ticketPrice??1000;
  $("event-form").classList.remove("hidden"); $("event-form").scrollIntoView({behavior:"smooth",block:"center"});
}
function closeEventForm(){ $("event-form").classList.add("hidden"); $("event-form").reset(); state.editingEventId=null; }
async function saveEvent(e) {
  e.preventDefault();
  const body={name:$("event-name").value.trim(),description:$("event-description").value.trim(),venue:$("event-venue").value.trim(),eventDate:$("event-date").value,totalSeats:Number($("event-total-seats").value),ticketPrice:Number($("event-price").value)};
  try {
    if(state.editingEventId) await api(`/api/events/${encodeURIComponent(state.editingEventId)}`,{method:"PUT",body});
    else await api("/api/events",{method:"POST",body});
    closeEventForm(); notify("Event saved successfully.","success"); loadEvents();
  } catch(e){notify(`Event could not be saved: ${e.message}`,"error");}
}
async function loadReservations() {
  try {
    const path=isCustomer()?"/api/reservations/my":"/api/reservations";
    const data=await api(path); state.reservations=Array.isArray(data)?data:(data?.content||data?.reservations||[]);
    renderReservations();
  } catch(e){notify(`Could not load reservations: ${e.message}`,"error");}
}
function renderReservations() {
    const list = $("reservations-list");

    $("reservations-empty").classList.toggle(
        "hidden",
        state.reservations.length > 0
    );

    if (!state.reservations.length) {
        list.innerHTML = "";
        return;
    }

    list.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Code</th>
                    <th>Customer</th>
                    <th>Event ID</th>
                    <th>Seats</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>

            <tbody>
                ${state.reservations.map(r => {
                    const id = r.id || r.reservationId;

                    const customer =
                        r.customer?.username ||
                        r.customerId ||
                        r.username ||
                        r.customerUsername ||
                        "-";

                    const event =
                        r.event?.name ||
                        r.eventName ||
                        r.eventId ||
                        "-";

                    const own = customer === state.user?.username;

                    const active =
                        String(r.status).toUpperCase() === "CONFIRMED";

                    const canCancel =
                        active && (
                            isAdmin() ||
                            (isCustomer() && own)
                        );

                    return `
                        <tr>
                            <td>${esc(r.code || r.reservationCode || id)}</td>
                            <td>${esc(customer)}</td>
                            <td>${esc(event)}</td>
                            <td>${esc(r.seats)}</td>
                            <td>${money(r.amount)}</td>

                            <td>
                                <span class="status ${active ? "" : "cancelled"}">
                                    ${esc(r.status)}
                                </span>
                            </td>

                            <td>
                                ${canCancel ? `
                                    <button
                                        class="btn btn-soft"
                                        data-update-res="${esc(id)}"
                                        data-seats="${esc(r.seats)}">
                                        Change seats
                                    </button>

                                    <button
                                        class="btn btn-danger"
                                        data-cancel-res="${esc(id)}">
                                        Cancel Reservation
                                    </button>
                                ` : ""}
                            </td>
                        </tr>
                    `;
                }).join("")}
            </tbody>
        </table>
    `;
}
async function loadUsers() {
  try { const data=await api("/api/admin/users"); state.users=Array.isArray(data)?data:(data?.content||data?.users||[]); renderUsers(); }
  catch(e){notify(`Could not load users: ${e.message}`,"error");}
}
function renderUsers() {
  $("users-list").innerHTML=`<table><thead><tr><th>Username</th><th>Full name</th><th>Role</th><th>Actions</th></tr></thead><tbody>${state.users.map(u=>{
    const id=u.id||u.userId; return `<tr><td>${esc(u.username)}</td><td>${esc(u.fullName)}</td><td>${esc(u.role)}</td><td><button class="btn btn-soft" data-edit-user="${esc(id)}">Edit</button> <button class="btn btn-ghost" data-delete-user="${esc(id)}">Delete</button></td></tr>`;
  }).join("")}</tbody></table>`;
}
async function loadStats() {
  try { const s=await api("/api/admin/stats"); const fields=[["Users",s.totalUsers??s.users],["Events",s.totalEvents??s.events],["Tickets sold",s.ticketsSold??s.totalTicketsSold],["Revenue",s.totalRevenue??s.revenue]];
    $("stats-list").innerHTML=fields.map(([label,value])=>`<div class="stat-card"><span>${esc(label)}</span><strong>${label==="Revenue"?money(value):esc(value??0)}</strong></div>`).join("");
  } catch(e){notify(`Could not load statistics: ${e.message}`,"error");}
}

$("login-form").addEventListener("submit",async e=>{e.preventDefault();try{await login($("login-username").value.trim(),$("login-password").value);}catch(err){authMessage(err.message);}});
$("register-form").addEventListener("submit",async e=>{
  e.preventDefault(); const body={username:$("register-username").value.trim(),fullName:$("register-fullname").value.trim(),password:$("register-password").value};
  try { await api("/api/auth/register",{method:"POST",body,token:""}); authMessage("Registration successful. You can now log in.",true); document.querySelector('[data-auth-tab="login"]').click(); $("login-username").value=body.username; }
  catch(err){authMessage(err.message);}
});
document.querySelectorAll("[data-auth-tab]").forEach(b=>b.addEventListener("click",()=>showAuth(b.dataset.authTab)));
document.querySelectorAll("[data-demo]").forEach(b=>b.addEventListener("click",()=>{const d=DEMO_ACCOUNTS[b.dataset.demo];$("login-username").value=d.username;$("login-password").value=d.password;}));
$("logout-btn").addEventListener("click",()=>logout());
document.querySelectorAll(".nav-tab").forEach(b=>b.addEventListener("click",()=>activatePanel(b.dataset.panel)));
$("show-event-form").addEventListener("click",()=>openEventForm()); $("close-event-form").addEventListener("click",closeEventForm); $("cancel-event-form").addEventListener("click",closeEventForm);
$("event-form").addEventListener("submit",saveEvent); $("search-events").addEventListener("click",loadEvents); $("refresh-events").addEventListener("click",loadEvents); $("event-search").addEventListener("keydown",e=>{if(e.key==="Enter")loadEvents();});
$("refresh-reservations").addEventListener("click",loadReservations); $("refresh-users").addEventListener("click",loadUsers); $("refresh-stats").addEventListener("click",loadStats);

document.addEventListener("click",async e=>{
  const book=e.target.closest("[data-book]"); if(book){
    const seats=prompt("How many seats would you like to book? (1–10)","2"); if(seats===null)return;
    const n=Number(seats); if(!Number.isInteger(n)||n<1||n>10){notify("Please enter a whole number from 1 to 10.","error");return;}
    try{await api("/api/reservations",{method:"POST",body:{eventId:book.dataset.book,seats:n}});notify("Reservation created.","success");loadEvents();}catch(err){notify(err.message,"error");} return;
  }
  const edit=e.target.closest("[data-edit-event]"); if(edit){const ev=state.events.find(x=>(x.id||x.eventId)===edit.dataset.editEvent);openEventForm(ev);return;}
  const del=e.target.closest("[data-delete-event]"); if(del&&confirm("Delete this event?")){try{await api(`/api/events/${encodeURIComponent(del.dataset.deleteEvent)}`,{method:"DELETE"});notify("Event deleted.","success");loadEvents();}catch(err){notify(err.message,"error");}return;}
  const cancel=e.target.closest("[data-cancel-res]"); if(cancel&&confirm("Cancel this reservation? Seats will be released.")){try{await api(`/api/reservations/${encodeURIComponent(cancel.dataset.cancelRes)}`,{method:"DELETE"});notify("Reservation cancelled.","success");loadReservations();loadEvents();}catch(err){notify(err.message,"error");}return;}
  const update=e.target.closest("[data-update-res]"); if(update){const seats=prompt("Enter the new total number of seats (1–10):",update.dataset.seats);if(seats===null)return;const n=Number(seats);if(!Number.isInteger(n)||n<1||n>10){notify("Enter a whole number from 1 to 10.","error");return;}try{await api(`/api/reservations/${encodeURIComponent(update.dataset.updateRes)}`,{method:"PUT",body:{seats:n}});notify("Reservation updated.","success");loadReservations();loadEvents();}catch(err){notify(err.message,"error");}return;}
  const eu=e.target.closest("[data-edit-user]"); if(eu){const u=state.users.find(x=>(x.id||x.userId)===eu.dataset.editUser);if(!u)return;const fullName=prompt("Enter updated full name:",u.fullName||"");if(fullName===null)return;const roleValue=prompt("Enter role (ADMIN, ORGANIZER, CUSTOMER):",u.role||"CUSTOMER");if(roleValue===null)return;const password=prompt("New password (leave blank to keep current password):","");const body={fullName,role:roleValue};if(password)body.password=password;try{await api(`/api/admin/users/${encodeURIComponent(eu.dataset.editUser)}`,{method:"PUT",body});notify("User updated.","success");loadUsers();}catch(err){notify(err.message,"error");}return;}
  const du=e.target.closest("[data-delete-user]");if(du&&confirm("Delete this user?")){try{await api(`/api/admin/users/${encodeURIComponent(du.dataset.deleteUser)}`,{method:"DELETE"});notify("User deleted.","success");loadUsers();}catch(err){notify(err.message,"error");}return;}
});

// Restore a previously stored token, then validate it with /api/auth/me.
(async()=>{if(state.token){try{state.user=await api("/api/auth/me");showApp();}catch{logout();}}else showAuth();})();
