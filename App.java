import React, { useState, useEffect } from "react";
import {
  Users,
  ShieldAlert,
  Award,
  CalendarCheck,
  TrendingDown,
  Wrench,
  DollarSign,
  Play,
  CheckCircle,
  Clock,
  Send,
  Plus,
  AlertCircle,
  FileCode,
  Sparkles,
  Terminal,
  Activity,
  Layers,
  ChevronRight,
  RefreshCw,
  Copy,
  Check,
  Cpu,
  GitBranch,
  X,
  FileText,
  Lock,
  Search
} from "lucide-react";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line
} from "recharts";

import { javaSources, JavaFile } from "./data/javaSources";
import { Employee, Task, AttendanceRecord, Workflow, UserRole } from "./types";

const COLORS = ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#ec4899"];

export default function App() {
  // Application State backed by actual wrapper APIs
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [attendance, setAttendance] = useState<AttendanceRecord[]>([]);
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  
  // Simulation Active State
  const [activeRole, setActiveRole] = useState<UserRole>("CEO");
  const [selectedJavaFile, setSelectedJavaFile] = useState<JavaFile>(javaSources[0]);
  const [modifiedCode, setModifiedCode] = useState<string>(javaSources[0].code);

  // API loading statuses
  const [loading, setLoading] = useState(true);
  const [apiError, setApiError] = useState<string | null>(null);

  // Form states
  const [newAttendanceRemark, setNewAttendanceRemark] = useState("");
  const [attendanceSuccess, setAttendanceSuccess] = useState(false);
  
  // Create Task form state
  const [newTaskTitle, setNewTaskTitle] = useState("");
  const [newTaskPriority, setNewTaskPriority] = useState<"LOW" | "MEDIUM" | "HIGH">("MEDIUM");
  const [newTaskRole, setNewTaskRole] = useState("Trainer");

  // Create Workflow custom builder state
  const [newWfName, setNewWfName] = useState("");
  const [newWfTrigger, setNewWfTrigger] = useState("");
  const [newWfAction, setNewWfAction] = useState("");
  const [newWfJavaCode, setNewWfJavaCode] = useState("");

  // AI assistant states
  const [aiPrompt, setAiPrompt] = useState("");
  const [aiResponse, setAiResponse] = useState("");
  const [aiLoading, setAiLoading] = useState(false);
  const [aiError, setAiError] = useState("");
  
  // Developer CLI & Logs state
  const [consoleLogs, setConsoleLogs] = useState<string[]>([
    "[Spring Boot Context] Bootstrapping ApplicationContext...",
    "[Hibernate] Found H2 Database in-memory dialect",
    "[Spring Security] FilterChain configured with strict pre-authorization rules.",
    "[JVM Server] Wrapper fully listening on port 3000.",
  ]);

  // Toast notifier
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const activeUser = {
    "CEO": { name: "Dinesh Kumar", role: "CEO | ERP SuperAdmin", avatar: "DK" },
    "HR Manager": { name: "Ananya S.", role: "HR Manager | Talent Admin", avatar: "AS" },
    "Trainer (Employee)": { name: "Rohit Sharma", role: "Lead Trainer | Trainer Workspace", avatar: "RS" },
    "Project Manager": { name: "Vikram Rathore", role: "Project Manager | Scrum Lead", avatar: "VR" },
    "Coordinator": { name: "Sneha Patel", role: "Coordinator | Logistics Lead", avatar: "SP" },
    "Operations Specialist": { name: "Amit Shah", role: "Operations Specialist | Workflow Engineer", avatar: "AS" },
    "Finance Manager": { name: "Riya Sen", role: "Finance Manager | Auditor", avatar: "RS" }
  }[activeRole] || { name: "Robert J. Sullivan", role: "ERP Admin", avatar: "RS" };

  // Fetch initial data from full stack API routes
  const fetchData = async () => {
    try {
      setLoading(true);
      const [resEmp, resTask, resAtt, resWf] = await Promise.all([
        fetch("/api/employees"),
        fetch("/api/tasks"),
        fetch("/api/attendance"),
        fetch("/api/workflows")
      ]);

      if (!resEmp.ok || !resTask.ok || !resAtt.ok || !resWf.ok) {
        throw new Error("Spring DB endpoint reported failure.");
      }

      const dataEmp = await resEmp.json();
      const dataTask = await resTask.json();
      const dataAtt = await resAtt.json();
      const dataWf = await resWf.json();

      setEmployees(dataEmp);
      setTasks(dataTask);
      setAttendance(dataAtt);
      setWorkflows(dataWf);
      setApiError(null);
    } catch (err: any) {
      console.error(err);
      setApiError("Database Sync Error: Unable to gather live ERP state.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Sync edited code when selected file changes
  useEffect(() => {
    setModifiedCode(selectedJavaFile.code);
  }, [selectedJavaFile]);

  // Push local logs to screen
  const logToConsole = (message: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setConsoleLogs((prev) => [...prev, `[${timestamp}] ${message}`]);
  };

  // Trigger Toast Notification
  const triggerToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage(null);
    }, 3500);
  };

  // API Call: Update employee configuration (HR action)
  const handleUpdateEmployee = async (id: string, updates: Partial<Employee>) => {
    try {
      const response = await fetch("/api/employees/update", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id, ...updates }),
      });
      if (response.ok) {
        const result = await response.json();
        setEmployees((prev) =>
          prev.map((emp) => (emp.id === id ? { ...emp, ...updates } : emp))
        );
        logToConsole(`JPA Query executed: UPDATE Employee SET rating='${updates.rating || ""}', workload=${updates.workload || 0} WHERE id='${id}'`);
        triggerToast("JPA persist transaction completed.");
      }
    } catch (err) {
      triggerToast("Failed to update Employee JPA state.");
    }
  };

  // API Call: Create a project task (PM action)
  const handleCreateTask = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTaskTitle.trim()) return;

    try {
      const response = await fetch("/api/tasks/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title: newTaskTitle,
          assigneeName: "Rohit Sharma", // Defaults to trainer Rohit for visual verification
          priority: newTaskPriority,
          roleRequired: newTaskRole,
        }),
      });

      if (response.ok) {
        const newTaskData = await response.json();
        setTasks((prev) => [...prev, newTaskData]);
        setNewTaskTitle("");
        logToConsole(`Spring Event: TaskCreatedEvent broadcast. Key: ${newTaskData.id}`);
        triggerToast(`Task ${newTaskData.id} created successfully.`);
      }
    } catch (err) {
      triggerToast("Error triggering Spring insert on tasks.");
    }
  };

  // API Call: Update task status (PM / Trainer action)
  const handleUpdateTaskStatus = async (id: string, status: "TODO" | "IN_PROGRESS" | "DONE") => {
    try {
      const response = await fetch("/api/tasks/update-status", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id, status }),
      });
      if (response.ok) {
        setTasks((prev) =>
          prev.map((task) => (task.id === id ? { ...task, status } : task))
        );
        logToConsole(`Transactional Update: Task ${id} transitioned to state ${status}`);
        triggerToast(`Task updated to ${status}`);
      }
    } catch (err) {
      triggerToast("Failed to shift task status.");
    }
  };

  // API Call: Attendance Check In (Trainer action)
  const handleAttendanceCheckIn = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await fetch("/api/attendance/check-in", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          empName: "Rohit Sharma",
          remark: newAttendanceRemark || "Checked in via trainer client dashboard",
        }),
      });
      if (response.ok) {
        const newRecord = await response.json();
        setAttendance((prev) => [newRecord, ...prev]);
        setNewAttendanceRemark("");
        setAttendanceSuccess(true);
        logToConsole(`Spring integration: Received CheckInDTO from TRAINER. Saved to database logs`);
        triggerToast("Check-In logged and auto-approved.");
        setTimeout(() => setAttendanceSuccess(false), 3000);
      }
    } catch (err) {
      triggerToast("Failed to register attendance.");
    }
  };

  // API Call: Toggle automated workflow configurations (Ops/Coordinator action)
  const handleToggleWorkflow = async (id: string) => {
    try {
      const response = await fetch("/api/workflows/toggle", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id }),
      });
      if (response.ok) {
        const result = await response.json();
        setWorkflows((prev) =>
          prev.map((wf) => (wf.id === id ? { ...wf, status: result.workflow.status } : wf))
        );
        logToConsole(`WorkflowEngine Alert: Pipeline sequence '${result.workflow.name}' marked ${result.workflow.status}`);
        triggerToast(`Workflow ${result.workflow.name} toggled.`);
      }
    } catch (err) {
      triggerToast("Could not modify workflow status.");
    }
  };

  // API Call: Create custom workflow route (Ops action)
  const handleCreateWorkflow = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newWfName || !newWfTrigger || !newWfAction) return;

    try {
      const cleanCode = newWfJavaCode || `workflowChain.addStep(new Validate${newWfName}Step()).addStep(new Notify${newWfName}Step());`;
      const response = await fetch("/api/workflows/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: newWfName,
          trigger: newWfTrigger,
          action: newWfAction,
          javaCode: cleanCode,
        }),
      });
      if (response.ok) {
        const newWf = await response.json();
        setWorkflows((prev) => [...prev, newWf]);
        setNewWfName("");
        setNewWfTrigger("");
        setNewWfAction("");
        setNewWfJavaCode("");
        logToConsole(`Spring Assembly: Loaded customized microservice chain block '${newWf.name}' into main Spring Container.`);
        triggerToast(`Workflow '${newWf.name}' is now active.`);
      }
    } catch (err) {
      triggerToast("Error compilation on new Java automation route.");
    }
  };

  // Server-side AI assist query using Gemini API
  const handleAskAI = async (recommendedPrompt?: string) => {
    const promptToSend = recommendedPrompt || aiPrompt;
    if (!promptToSend.trim()) return;

    setAiLoading(true);
    setAiError("");
    setAiResponse("");

    try {
      const res = await fetch("/api/gemini/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          prompt: promptToSend,
          currentRole: activeRole,
          contextCode: modifiedCode,
        }),
      });

      const data = await res.json();
      if (data.error) {
        setAiError(data.error);
        logToConsole(`AI Compile warning: ${data.error}`);
      } else {
        setAiResponse(data.text);
        logToConsole(`AI Assistant action: Successfully compiled recommendation on requested query.`);
      }
    } catch (err: any) {
      setAiError("Connection Timeout or missing API key standard validation on port 3000.");
      logToConsole("AI Assistant failed. Check if process.env.GEMINI_API_KEY is configured.");
    } finally {
      setAiLoading(false);
    }
  };

  // Live action to copy AI suggested code into the main IDE text view for testing
  const handleInjectCodeFromAI = () => {
    if (!aiResponse) return;
    // Attempt to extract markdown java blocks if present
    const regex = /```java([\s\S]*?)```/i;
    const match = aiResponse.match(regex);
    if (match && match[1]) {
      setModifiedCode(match[1].trim());
      logToConsole(`Refactored IDE Context: Overwrote current source with AI suggested Java module.`);
      triggerToast("Java context updated in IDE.");
    } else {
      // Inject directly
      setModifiedCode(aiResponse);
      logToConsole(`Refactored IDE Context: Applied full text recommendation.`);
      triggerToast("Code applied directly.");
    }
  };

  // Re-run system compile verification (simulate Maven Build plugin)
  const triggerMavenBuild = () => {
    logToConsole("MAVEN: Clean compiling target/classes...");
    logToConsole("MAVEN: Running SpringBoot JPA Audit Verify...");
    logToConsole("MAVEN: BUILD SUCCESS. Total duration: 1.481s");
    triggerToast("Spring context build succeeded.");
  };

  // Quick Action triggers for AI Questions
  const promptChips = [
    { label: "Spring Security Filter Chain configure", text: "Write matching security permit rules in Java for HR/Trainers using Spring Boot SecurityConfig" },
    { label: "JPA ManyToOne Relationship Map", text: "Show how to map an @ManyToOne relationship between Attendance.java and Employee.java with proper Cascade types" },
    { label: "JUnit validation Controller tests", text: "Draft robust MockMvc tests to test roles in EmployeeController.java" },
    { label: "Build Workflow pipeline automation logic", text: "Create a custom Java Workflow step that implements automated payroll constraints alert validation" }
  ];

  return (
    <div className="flex flex-col lg:flex-row min-h-screen bg-slate-50 font-sans text-slate-900 selection:bg-blue-600 selection:text-white">
      
      {/* Dynamic Toast Message */}
      {toastMessage && (
        <div className="fixed top-4 right-4 z-50 bg-slate-950 text-white px-4 py-3 rounded-lg shadow-2xl flex items-center space-x-2 border border-slate-800 animate-bounce">
          <Activity className="h-5 w-5 text-blue-500 animate-spin" />
          <span className="font-semibold text-xs tracking-tight">{toastMessage}</span>
        </div>
      )}

      {/* LEFT COLUMN: Sidebar Security & Simulator Navigation */}
      <aside className="lg:w-72 bg-slate-900 flex flex-col shrink-0 text-slate-100 border-r border-slate-800">
        
        {/* Sidebar Logo Header */}
        <div className="p-6 flex items-center justify-between border-b border-slate-800/80">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-blue-600 rounded-md flex items-center justify-center font-bold text-white font-mono shadow-md shadow-blue-500/20">
              ☕
            </div>
            <div>
              <span className="text-white font-bold font-display text-base tracking-tight block">JavaPortal AI</span>
              <span className="text-[10px] text-slate-450 font-mono">Simulated ERP v2.4</span>
            </div>
          </div>
          <span className="text-[10px] uppercase font-mono px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
            RBAC
          </span>
        </div>

        {/* Dynamic Security Switchers List */}
        <div className="flex-1 px-4 py-5 space-y-5 overflow-y-auto">
          <div>
            <span className="text-[10px] text-slate-500 uppercase font-black tracking-widest block px-3 mb-2 font-display">
              Change Security Context
            </span>
            <nav className="space-y-1">
              {[
                { role: "CEO", label: "CEO Command Console", icon: Award, color: "text-amber-400" },
                { role: "HR Manager", label: "HR Portal & Ratings", icon: Users, color: "text-blue-400" },
                { role: "Trainer (Employee)", label: "My Check-Ins & Tasks", icon: CalendarCheck, color: "text-emerald-400" },
                { role: "Project Manager", label: "PM Kanban Board", icon: Layers, color: "text-purple-400" },
                { role: "Coordinator", label: "Coordinator Desk", icon: GitBranch, color: "text-teal-400" },
                { role: "Operations Specialist", label: "Operations Automator", icon: Wrench, color: "text-pink-400" },
                { role: "Finance Manager", label: "Finance & Payroll Logs", icon: DollarSign, color: "text-teal-450" }
              ].map((item) => {
                const IconComponent = item.icon;
                const isSelected = activeRole === item.role;
                return (
                  <button
                    key={item.role}
                    id={`sidebar-role-${item.role.replace(/\s+/g, '-')}`}
                    onClick={() => {
                      setActiveRole(item.role as UserRole);
                      logToConsole(`RBAC Session Authorization: User identity mutated to [Role: ROLE_${item.role.toUpperCase().replace(/\s+/g, '_')}]`);
                      triggerToast(`Switched view to simulated credentials: ${item.role}`);
                    }}
                    className={`w-full flex items-center justify-between px-3 py-2.5 rounded-lg text-xs font-semibold cursor-pointer transition-all ${
                      isSelected
                        ? "bg-blue-600 text-white shadow-md shadow-blue-500/10 border border-blue-500/20"
                        : "text-slate-400 hover:text-white hover:bg-slate-800/85"
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <IconComponent className={`h-4 w-4 shrink-0 ${isSelected ? "text-white" : item.color}`} />
                      <span className="text-left leading-tight">{item.label}</span>
                    </div>
                    {isSelected && <ChevronRight className="h-3.5 w-3.5 shrink-0 opacity-80" />}
                  </button>
                );
              })}
            </nav>
          </div>

          <div className="border-t border-slate-800/85 pt-4">
            <span className="text-[10px] text-slate-500 uppercase font-bold tracking-widest block px-3 mb-2">
              Simulation Assets
            </span>
            <div className="space-y-2 px-3">
              <button
                onClick={triggerMavenBuild}
                className="w-full bg-slate-800 hover:bg-slate-750 border border-slate-700 hover:border-slate-650 text-slate-200 text-[11px] font-mono py-1.5 px-2.5 rounded-md flex items-center justify-center gap-1.5 cursor-pointer transition-all"
              >
                <Cpu className="h-3.5 w-3.5 text-blue-400" />
                <span>mvn Clean Compile</span>
              </button>
            </div>
          </div>
        </div>

        {/* Active Session Context Box inside Aside footer */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/40">
          <div className="bg-slate-800/60 border border-slate-750 rounded-lg p-3">
            <div className="text-[9px] text-slate-500 uppercase font-black tracking-widest mb-1.5 font-display">
              ACTIVE JVM USER
            </div>
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20 font-bold text-xs flex items-center justify-center font-mono font-sans shrink-0">
                {activeUser.avatar}
              </div>
              <div className="overflow-hidden p-0.5">
                <div className="text-xs font-bold text-slate-200 truncate">{activeUser.name}</div>
                <div className="text-[10px] text-slate-400 truncate">{activeUser.role}</div>
              </div>
            </div>
          </div>
        </div>

      </aside>

      {/* RIGHT COLUMN: Header, Dynamic Dashboard, Tab Tables & Code IDE Side by Side */}
      <main className="flex-1 flex flex-col min-h-screen overflow-x-hidden">
        
        {/* Main Sticky Header */}
        <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6 shrink-0 shadow-sm z-10">
          
          <div className="flex items-center gap-6">
            <h1 className="text-base font-bold text-slate-800 font-display tracking-tight flex items-center space-x-2">
              <span>Dynamic Workforce ERP Systems</span>
              <span className="text-[10px] text-slate-400 font-mono font-medium hidden sm:inline">
                | Spring Boot 3 - Hibernate 6 Stack
              </span>
            </h1>

            {/* Simulated Search bar */}
            <div className="relative hidden md:block select-none">
              <Search className="absolute left-3 top-2.5 h-3.5 w-3.5 text-slate-400" />
              <input
                type="text"
                placeholder="Search database fields, DTO files..."
                className="pl-8 pr-4 py-1 bg-slate-100 placeholder:text-slate-400 text-xs rounded-full border-none w-56 focus:outline-none focus:ring-1 focus:ring-blue-500 transition-all text-slate-800 cursor-not-allowed"
                disabled
              />
            </div>
          </div>

          <div className="flex items-center gap-4">
            
            {/* Quick reduction impact stat pill */}
            <div className="flex items-center space-x-2 bg-emerald-50 text-emerald-800 border border-emerald-110 px-3 py-1 rounded-full text-xs">
              <Activity className="h-3.5 w-3.5 text-emerald-600 animate-pulse" />
              <span className="font-bold whitespace-nowrap">Reduced Admin Overhead by 40%</span>
            </div>

            <div className="flex items-center gap-3 pl-4 border-l border-slate-200">
              <div className="text-right hidden sm:block">
                <div className="text-xs font-semibold text-slate-800 font-display">Spring Session ID</div>
                <div className="text-[9px] text-slate-400 font-mono tracking-tight">JSESSIONID=E5B94AA8712C</div>
              </div>
            </div>

          </div>

        </header>

        {/* Primary Page Workspace Content */}
        <div className="p-6 space-y-6 flex-1 flex flex-col justify-between max-w-[1440px]">
          
          {/* Top Operational Stats Row */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            
            {/* Card 1: Reduction Value */}
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                Operational Impact
              </div>
              <div className="text-3xl font-black text-blue-600 font-display my-1">
                -40%
              </div>
              <div className="text-[11px] text-slate-500 font-medium">
                Admin paperwork reduced successfully
              </div>
            </div>

            {/* Card 2: Employees state */}
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                System Registry
              </div>
              <div className="text-3xl font-black text-slate-800 font-display my-1">
                {employees.length}
              </div>
              <div className="text-[11px] text-slate-500">
                Staff records active in H2 Database schema
              </div>
            </div>

            {/* Card 3: Metrics status */}
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                Pending Workloads
              </div>
              <div className="text-3xl font-black text-amber-600 font-display my-1">
                {tasks.filter(t => t.status !== "DONE").length}
              </div>
              <div className="text-[11px] text-slate-500">
                Active tasks awaiting Trainer execution
              </div>
            </div>

            {/* Card 4: Automators */}
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                Active Automations
              </div>
              <div className="text-3xl font-black text-emerald-600 font-display my-1">
                {workflows.filter(w => w.status === "ENABLED").length} Flows
              </div>
              <div className="text-[11px] text-slate-500">
                Automatic workflow chains currently enabled
              </div>
            </div>

          </div>

          {/* Main workspace splits */}
          <div className="grid grid-cols-1 xl:grid-cols-12 gap-6 items-start">
            
            {/* Live Enterprise Client Workspace Panel (Left Side of internal main layout) */}
            <div className="xl:col-span-7 flex flex-col space-y-5">
              
              {/* Active Worksheets / Views according to the chosen role */}
              <div className="flex-1 bg-white text-slate-900 rounded-2xl p-6 shadow-xl border border-slate-200/80 flex flex-col justify-between min-h-[500px]">
            
            <div>
              {/* Dynamic Header on Client Application based on selected role */}
              <div className="flex items-center justify-between border-b pb-4 mb-5 border-slate-100">
                <div className="flex items-center space-x-3">
                  <div className="p-2.5 rounded-xl bg-slate-50 border shadow-sm">
                    {activeRole === "CEO" && <Award className="h-6 w-6 text-amber-500" />}
                    {activeRole === "HR Manager" && <Users className="h-6 w-6 text-slate-700" />}
                    {activeRole === "Trainer (Employee)" && <CalendarCheck className="h-6 w-6 text-blue-500" />}
                    {activeRole === "Project Manager" && <Layers className="h-6 w-6 text-purple-500" />}
                    {activeRole === "Coordinator" && <GitBranch className="h-6 w-6 text-emerald-500" />}
                    {activeRole === "Operations Specialist" && <Wrench className="h-6 w-6 text-slate-800" />}
                    {activeRole === "Finance Manager" && <DollarSign className="h-6 w-6 text-teal-600" />}
                  </div>
                  <div>
                    <span className="text-xs uppercase font-bold tracking-wider text-slate-400 block font-display">AUTHORIZED MODULE</span>
                    <h2 className="text-lg font-bold text-slate-800 font-display flex items-center space-x-1.5">
                      <span>{activeRole} Active Workspace</span>
                      <span className="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded font-mono font-normal">
                        ROLE_{activeRole.toUpperCase().replace(/\((.*?)\)/, "").trim().replace(/\s+/g, "_")}
                      </span>
                    </h2>
                  </div>
                </div>

                <div className="text-right hidden sm:block">
                  <span className="text-xs text-slate-400 font-mono">Simulated Spring Session ID</span>
                  <span className="block text-xs font-mono text-slate-700 font-bold">JSESSIONID=E5B94AA8712C</span>
                </div>
              </div>

              {/* 1. CEO Workspace - Smart Dashboard Analytics and Strategic Adjustments */}
              {activeRole === "CEO" && (
                <div className="space-y-6">
                  {/* Strategic summary */}
                  <div className="p-4 bg-gradient-to-r from-slate-900 to-slate-800 text-slate-100 rounded-xl flex flex-col sm:flex-row justify-between items-start sm:items-center shadow">
                    <div>
                      <h4 className="font-bold text-sm text-amber-400">CEO Meta-Analytics Control</h4>
                      <p className="text-xs text-slate-300 mt-1">
                        Review overall organizational synergy, workloads, and real-time Java microservice integration logs.
                      </p>
                    </div>
                    <button
                      id="strategic-recompile-btn"
                      onClick={triggerMavenBuild}
                      className="mt-3 sm:mt-0 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs py-1.5 px-3 rounded-lg shadow cursor-pointer transition-all"
                    >
                      Audit Maven DB Project
                    </button>
                  </div>

                  {/* Recharts Bar: Overarching work allocation & performance indices */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="p-4 border rounded-xl bg-slate-50">
                      <h3 className="text-xs font-bold text-slate-600 mb-3 uppercase tracking-wider">Workload Allocation by Employee (%)</h3>
                      <div className="h-44">
                        <ResponsiveContainer width="100%" height="100%">
                          <BarChart data={employees}>
                            <XAxis dataKey="name" tick={{ fontSize: 9 }} stroke="#64748b" />
                            <YAxis tick={{ fontSize: 9 }} stroke="#64748b" />
                            <Tooltip wrapperStyle={{ fontSize: 11 }} />
                            <Bar dataKey="workload" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                          </BarChart>
                        </ResponsiveContainer>
                      </div>
                    </div>

                    <div className="p-4 border rounded-xl bg-slate-50">
                      <h3 className="text-xs font-bold text-slate-600 mb-3 uppercase tracking-wider">Active Attendance Tracking (%)</h3>
                      <div className="h-44">
                        <ResponsiveContainer width="100%" height="100%">
                          <LineChart data={employees}>
                            <XAxis dataKey="name" tick={{ fontSize: 9 }} stroke="#64748b" />
                            <YAxis domain={[80, 100]} tick={{ fontSize: 9 }} stroke="#64748b" />
                            <Tooltip wrapperStyle={{ fontSize: 11 }} />
                            <Line type="monotone" dataKey="attendance" stroke="#10b981" strokeWidth={2} />
                          </LineChart>
                        </ResponsiveContainer>
                      </div>
                    </div>
                  </div>

                  {/* Operational impact matrix */}
                  <div className="border rounded-xl overflow-hidden shadow-sm">
                    <div className="bg-slate-100 px-4 py-2 border-b text-xs font-bold text-slate-700">
                      Strategic Corporate KPI Dashboard
                    </div>
                    <div className="grid grid-cols-3 divide-x divide-y md:divide-y-0 text-center bg-white">
                      <div className="p-4">
                        <span className="text-2xl font-black text-blue-600 font-display">40%</span>
                        <p className="text-xs font-bold text-slate-600 mt-1">Admin Reduction</p>
                      </div>
                      <div className="p-4">
                        <span className="text-2xl font-black text-emerald-600 font-display">94.8%</span>
                        <p className="text-xs font-bold text-slate-600 mt-1">Avg Attendance Rate</p>
                      </div>
                      <div className="p-4">
                        <span className="text-2xl font-black text-purple-600 font-display">{tasks.filter(t => t.status === "DONE").length}/{tasks.length}</span>
                        <p className="text-xs font-bold text-slate-600 mt-1">Java Class Tasks</p>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* 2. HR Manager Workspace - Employee Database & Action Ratings */}
              {activeRole === "HR Manager" && (
                <div className="space-y-4">
                  <div className="bg-blue-50/50 border border-blue-200 p-3.5 rounded-xl text-xs text-blue-800 leading-relaxed">
                    <span className="font-bold">HR Authorization Token verified:</span> Modify employee details, update ratings (Outstanding, Execellent, Average, Below Average), and adjust allocations. These actions trigger server-side JPA entity persistence.
                  </div>

                  <div className="overflow-x-auto border rounded-xl shadow-sm">
                    <table className="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr className="bg-slate-50 border-b text-slate-600 font-bold">
                          <th className="p-3">ID</th>
                          <th className="p-3">Employee Name</th>
                          <th className="p-3">Department/Role</th>
                          <th className="p-3 text-center">Workload Allocation</th>
                          <th className="p-3 text-center">Performance Rating</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y text-slate-700">
                        {employees.map((emp) => (
                          <tr key={emp.id} className="hover:bg-slate-50/70 transition-colors">
                            <td className="p-3 font-mono font-bold text-slate-500">{emp.id}</td>
                            <td className="p-3 font-medium text-slate-900">{emp.name}</td>
                            <td className="p-3 text-slate-600">
                              <div>{emp.department}</div>
                              <div className="text-[10px] text-slate-400 italic">{emp.role}</div>
                            </td>
                            <td className="p-3 text-center">
                              <div className="flex items-center justify-center space-x-1">
                                <span className="font-semibold block w-8">{emp.workload}%</span>
                                <input
                                  type="range"
                                  min="10"
                                  max="100"
                                  step="5"
                                  value={emp.workload}
                                  onChange={(e) => handleUpdateEmployee(emp.id, { workload: Number(e.target.value) })}
                                  className="w-16 cursor-pointer accent-blue-600"
                                />
                              </div>
                            </td>
                            <td className="p-3 text-center">
                              <select
                                value={emp.rating}
                                onChange={(e) => handleUpdateEmployee(emp.id, { rating: e.target.value })}
                                className="border rounded bg-white p-1 text-xs font-bold text-slate-800 focus:ring focus:ring-blue-100"
                              >
                                <option value="O">O (Outstanding)</option>
                                <option value="E">E (Excellent)</option>
                                <option value="A">A (Average)</option>
                                <option value="M">M (Marginal)</option>
                                <option value="U">U (Unsatisfactory)</option>
                              </select>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* 3. Trainer / Employee Workspace - Attendance check-ins and personal task assignments */}
              {activeRole === "Trainer (Employee)" && (
                <div className="space-y-6">
                  {/* Geo-fenced Attendance log widget */}
                  <div className="p-4 border border-blue-100 bg-blue-50/30 rounded-xl">
                    <h3 className="text-xs font-bold text-blue-900 uppercase tracking-wider mb-2 flex items-center space-x-1">
                      <Clock className="h-4 w-4" />
                      <span>Employee Daily Check-In Punch (Simulated Geo-Fence)</span>
                    </h3>
                    <p className="text-xs text-slate-600 mb-3">
                      Trainer geo-location is automatically derived. Submit check-in details to record in JPA database.
                    </p>

                    <form onSubmit={handleAttendanceCheckIn} className="flex flex-col sm:flex-row gap-2">
                      <input
                        type="text"
                        placeholder="Add remark (e.g., 'In classroom 401', 'Remote connection')"
                        value={newAttendanceRemark}
                        onChange={(e) => setNewAttendanceRemark(e.target.value)}
                        className="flex-1 text-xs border bg-white rounded-lg p-2 focus:ring-2 focus:ring-blue-500 focus:outline-none"
                      />
                      <button
                        type="submit"
                        className="bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs px-4 py-2 rounded-lg cursor-pointer transition-all"
                      >
                        Punch-In Attendance
                      </button>
                    </form>

                    {attendanceSuccess && (
                      <div className="mt-2 text-xs text-green-600 font-bold flex items-center space-x-1 animate-pulse">
                        <Check className="h-4 w-4" />
                        <span>Attendance registered under Rohit Sharma (TRAINER)</span>
                      </div>
                    )}
                  </div>

                  {/* List of assigned tasks */}
                  <div className="space-y-3">
                    <div className="flex justify-between items-center sm:pr-2">
                      <h4 className="text-xs font-bold text-slate-600 uppercase tracking-wider">Assigned Task Pipeline for Trainers</h4>
                      <span className="text-[10px] text-slate-500 font-mono">ROLE_TRAINER filter active</span>
                    </div>

                    <div className="space-y-2">
                      {tasks
                        .filter((t) => t.roleRequired === "Trainer")
                        .map((t) => (
                          <div
                            key={t.id}
                            className={`p-3 border rounded-xl flex items-center justify-between transition-all ${
                              t.status === "DONE"
                                ? "bg-slate-50/50 border-slate-100 line-through text-slate-400"
                                : "bg-white border-slate-200"
                            }`}
                          >
                            <div className="flex items-center space-x-2.5">
                              <input
                                type="checkbox"
                                checked={t.status === "DONE"}
                                onChange={(e) =>
                                  handleUpdateTaskStatus(t.id, e.target.checked ? "DONE" : "IN_PROGRESS")
                                }
                                className="h-4.5 w-4.5 rounded text-blue-600 cursor-pointer accent-blue-600"
                              />
                              <div>
                                <span className="font-mono text-[10px] text-slate-400 font-bold block">{t.id}</span>
                                <span className="text-xs font-medium text-slate-800">{t.title}</span>
                              </div>
                            </div>

                            <span
                              className={`text-[9px] px-2 py-0.5 rounded-full uppercase font-bold ${
                                t.priority === "HIGH"
                                  ? "bg-red-50 text-red-600"
                                  : t.priority === "MEDIUM"
                                  ? "bg-amber-50 text-amber-600"
                                  : "bg-blue-50 text-blue-600"
                              }`}
                            >
                              {t.priority}
                            </span>
                          </div>
                        ))}
                    </div>
                  </div>
                </div>
              )}

              {/* 4. Project Manager Workspace - Task Board Kanban */}
              {activeRole === "Project Manager" && (
                <div className="space-y-5">
                  {/* Create task inline form */}
                  <form onSubmit={handleCreateTask} className="p-4 bg-purple-50/30 border border-purple-100 rounded-xl space-y-3">
                    <h4 className="text-xs font-bold text-purple-900 uppercase tracking-wider">Deploy New Enterprise Task</h4>
                    <div className="grid grid-cols-1 md:grid-cols-12 gap-2">
                      <input
                        type="text"
                        placeholder="Task details (e.g. Write SecurityFilters tests...)"
                        value={newTaskTitle}
                        onChange={(e) => setNewTaskTitle(e.target.value)}
                        className="md:col-span-6 text-xs bg-white border rounded-lg p-2 focus:ring focus:ring-purple-100"
                      />
                      <select
                        value={newTaskPriority}
                        onChange={(e) => setNewTaskPriority(e.target.value as any)}
                        className="md:col-span-3 text-xs bg-white border rounded-lg p-2 focus:ring focus:ring-purple-100 font-bold"
                      >
                        <option value="LOW">Low Priority</option>
                        <option value="MEDIUM">Medium Priority</option>
                        <option value="HIGH">High Priority</option>
                      </select>
                      <select
                        value={newTaskRole}
                        onChange={(e) => setNewTaskRole(e.target.value)}
                        className="md:col-span-3 text-xs bg-white border rounded-lg p-2 focus:ring focus:ring-purple-100"
                      >
                        <option value="Trainer">Trainer Required</option>
                        <option value="HR Manager">HR Required</option>
                        <option value="Finance Manager">Finance Required</option>
                        <option value="Operations Specialist">Ops Required</option>
                      </select>
                    </div>
                    <div className="flex justify-end pt-1">
                      <button
                        type="submit"
                        className="bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs py-1.5 px-4 rounded-lg cursor-pointer transition-all flex items-center space-x-1"
                      >
                        <Plus className="h-4 w-4" />
                        <span>Deploy Task Context</span>
                      </button>
                    </div>
                  </form>

                  {/* Task Board Columns */}
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                    {/* TODO Column */}
                    <div className="p-3 bg-slate-50 rounded-xl border border-slate-100 min-h-[160px]">
                      <h3 className="text-[11px] font-black uppercase text-slate-500 tracking-wider mb-2 flex justify-between">
                        <span>TO DO</span>
                        <span className="bg-slate-200 text-slate-700 px-1.5 py-0.2 rounded-full font-mono text-[9px]">{tasks.filter(t => t.status === "TODO").length}</span>
                      </h3>
                      <div className="space-y-2">
                        {tasks.filter(t => t.status === "TODO").map(t => (
                          <div key={t.id} className="p-3 bg-white border rounded-lg shadow-sm flex flex-col justify-between space-y-2">
                            <h4 className="text-xs font-semibold text-slate-800">{t.title}</h4>
                            <div className="flex justify-between items-center pt-1.5 border-t border-slate-50">
                              <span className="text-[9px] font-mono text-slate-400 uppercase font-black">{t.id}</span>
                              <button
                                onClick={() => handleUpdateTaskStatus(t.id, "IN_PROGRESS")}
                                className="text-[9px] bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold px-1.5 py-0.5 rounded cursor-pointer"
                              >
                                Start Work &rarr;
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* IN PROGRESS Column */}
                    <div className="p-3 bg-amber-50/40 rounded-xl border border-amber-100 min-h-[160px]">
                      <h3 className="text-[11px] font-black uppercase text-amber-700 tracking-wider mb-2 flex justify-between">
                        <span>IN PROGRESS</span>
                        <span className="bg-amber-100 text-amber-800 px-1.5 py-0.2 rounded-full font-mono text-[9px]">{tasks.filter(t => t.status === "IN_PROGRESS").length}</span>
                      </h3>
                      <div className="space-y-2">
                        {tasks.filter(t => t.status === "IN_PROGRESS").map(t => (
                          <div key={t.id} className="p-3 bg-white border rounded-lg shadow-sm flex flex-col justify-between space-y-2">
                            <h4 className="text-xs font-semibold text-slate-800">{t.title}</h4>
                            <div className="flex justify-between items-center pt-1.5 border-t border-slate-50">
                              <span className="text-[10px] text-blue-500 font-medium">@{t.assigneeName.split(" ")[0]}</span>
                              <button
                                onClick={() => handleUpdateTaskStatus(t.id, "DONE")}
                                className="text-[9px] bg-emerald-50 hover:bg-emerald-100 text-emerald-700 font-bold px-1.5 py-0.5 rounded cursor-pointer"
                              >
                                Finish Task
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* DONE Column */}
                    <div className="p-3 bg-emerald-50/30 rounded-xl border border-emerald-100 min-h-[160px]">
                      <h3 className="text-[11px] font-black uppercase text-emerald-700 tracking-wider mb-2 flex justify-between">
                        <span>DONE</span>
                        <span className="bg-emerald-100 text-emerald-800 px-1.5 py-0.2 rounded-full font-mono text-[9px]">{tasks.filter(t => t.status === "DONE").length}</span>
                      </h3>
                      <div className="space-y-2">
                        {tasks.filter(t => t.status === "DONE").map(t => (
                          <div key={t.id} className="p-3 bg-white border border-slate-100 text-slate-400 rounded-lg shadow-sm line-through">
                            <h4 className="text-xs font-medium">{t.title}</h4>
                            <span className="text-[9px] font-mono block mt-1 text-slate-300 font-bold">{t.id}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* 5. Coordinator Panel */}
              {activeRole === "Coordinator" && (
                <div className="space-y-4">
                  <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-900 rounded-xl">
                    <h3 className="font-bold text-sm">Workload Coordination Desk</h3>
                    <p className="text-xs mt-1 leading-relaxed">
                      Maintain direct, friction-free coordination of Trainer workload limits and automate standard approval steps.
                    </p>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="p-4 border rounded-xl">
                      <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Trainer Load Index</h4>
                      <ul className="space-y-2.5 text-xs">
                        <li className="flex justify-between items-center">
                          <span className="font-semibold text-slate-700">Audit Safety Cap:</span>
                          <span className="bg-red-100 text-red-800 font-bold px-2 py-0.5 rounded font-mono">80% MAX</span>
                        </li>
                        <li className="flex justify-between items-center">
                          <span className="font-semibold text-slate-700">Highest Active Allocation:</span>
                          <span className="font-bold font-mono">75% (Vikram Rathore)</span>
                        </li>
                        <li className="flex justify-between items-center">
                          <span className="font-semibold text-slate-700">Global Efficiency Offset:</span>
                          <span className="text-emerald-500 font-bold">-15% Reduced</span>
                        </li>
                      </ul>
                    </div>

                    <div className="p-4 border rounded-xl">
                      <h4 className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Live Logs Pipeline Alerts</h4>
                      <div className="space-y-2 text-[11px] font-mono">
                        <div className="text-blue-600 font-bold">[AUTO-LOG]: CheckInDTO registered success</div>
                        <div className="text-slate-500">[TRACE]: Hibernate: select count(*) from attendance...</div>
                        <div className="text-emerald-600 font-bold">[SPRING]: Active workflow engine online.</div>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* 6. Operations Specialist Workspace - Workflow Automation list and dynamic trigger generator */}
              {activeRole === "Operations Specialist" && (
                <div className="space-y-5">
                  <div className="flex justify-between items-center pr-1">
                    <h3 className="text-xs font-bold text-slate-500 uppercase tracking-wider">Loaded Spring Automations (JPA Interventions)</h3>
                    <span className="text-xs bg-emerald-50 border border-emerald-100 text-emerald-700 font-bold px-2 py-0.5 rounded-full flex items-center space-x-1">
                      <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                      <span>WorkflowEngine active</span>
                    </span>
                  </div>

                  {/* List of server workflows */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {workflows.map((wf) => (
                      <div key={wf.id} className="p-4 border rounded-xl bg-slate-50/50 flex flex-col justify-between">
                        <div>
                          <div className="flex justify-between items-start">
                            <h4 className="text-xs font-bold text-slate-800 font-mono italic">{wf.name}</h4>
                            <button
                              onClick={() => handleToggleWorkflow(wf.id)}
                              className={`text-[9px] font-bold px-2 py-0.5 rounded-full cursor-pointer transition-all ${
                                wf.status === "ENABLED"
                                  ? "bg-emerald-100 text-emerald-800"
                                  : "bg-red-100 text-red-800"
                              }`}
                            >
                              {wf.status}
                            </button>
                          </div>
                          <div className="mt-2 space-y-1">
                            <span className="text-[10px] block text-slate-500 font-mono leading-tight">
                              <strong>Trigger:</strong> {wf.trigger}
                            </span>
                            <span className="text-[10px] block text-slate-500 font-mono leading-tight">
                              <strong>Action:</strong> {wf.action}
                            </span>
                          </div>
                        </div>

                        <div className="mt-3 bg-slate-900 text-[9px] font-mono p-2 rounded text-blue-400 overflow-x-auto select-all">
                          {wf.javaCode}
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Create custom workflow builder form */}
                  <form onSubmit={handleCreateWorkflow} className="p-4 bg-slate-50 border border-slate-200/80 rounded-xl space-y-3">
                    <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center space-x-1">
                      <Terminal className="h-4 w-4 text-emerald-600" />
                      <span>Assemble Custom Automated Spring Process Block</span>
                    </h4>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-2">
                      <input
                        type="text"
                        placeholder="Workflow Class Name (e.g. VacationApprove)"
                        value={newWfName}
                        onChange={(e) => setNewWfName(e.target.value)}
                        className="text-xs bg-white border rounded-lg p-2 focus:ring focus:ring-blue-100 font-mono"
                      />
                      <input
                        type="text"
                        placeholder="Trigger condition (e.g. Submission of DTO)"
                        value={newWfTrigger}
                        onChange={(e) => setNewWfTrigger(e.target.value)}
                        className="text-xs bg-white border rounded-lg p-2 focus:ring"
                      />
                      <input
                        type="text"
                        placeholder="Action execution rule"
                        value={newWfAction}
                        onChange={(e) => setNewWfAction(e.target.value)}
                        className="text-xs bg-white border rounded-lg p-2 focus:ring"
                      />
                    </div>

                    <input
                      type="text"
                      placeholder="Optional Custom Java Pipeline instruction (e.g., pipeline.addStep(new EmailNotifyStep()))"
                      value={newWfJavaCode}
                      onChange={(e) => setNewWfJavaCode(e.target.value)}
                      className="w-full text-xs font-mono bg-white border rounded-lg p-2 focus:ring"
                    />

                    <div className="flex justify-end pt-1">
                      <button
                        type="submit"
                        className="bg-slate-800 hover:bg-slate-900 text-white font-bold text-xs py-1.5 px-4 rounded-lg cursor-pointer transition-all"
                      >
                        Compile & Inject Workflow
                      </button>
                    </div>
                  </form>
                </div>
              )}

              {/* 7. Finance Manager Workspace - Wages Spreadsheet Grid */}
              {activeRole === "Finance Manager" && (
                <div className="space-y-5">
                  <div className="p-4 bg-teal-50 border border-teal-200 text-teal-900 rounded-xl flex flex-col sm:flex-row justify-between items-start sm:items-center">
                    <div>
                      <h4 className="font-bold text-sm">Automated Microservice Disbursement Desk</h4>
                      <p className="text-xs text-slate-600 mt-1">
                        Execute verified payroll transactions and authorize Spring Scheduled salary checks instantly.
                      </p>
                    </div>
                    <button
                      onClick={() => {
                        logToConsole("FINANCE_SERVICE: Spawning multi-thread payroll ledger verification job...");
                        logToConsole("FINANCE_SERVICE: Disbursed salary pool successfully to 7 active corporate ledgers.");
                        triggerToast("Payroll auto-disbursement succeeded.");
                      }}
                      className="mt-3 sm:mt-0 bg-teal-600 hover:bg-teal-700 text-white font-bold text-xs py-1.5 px-4 rounded-lg shadow cursor-pointer transition-all"
                    >
                      Disburse Q2 Salary Pool
                    </button>
                  </div>

                  <div className="overflow-x-auto border rounded-xl shadow-sm">
                    <table className="w-full text-left border-collapse text-xs">
                      <thead>
                        <tr className="bg-slate-50 border-b text-slate-600 font-bold">
                          <th className="p-3">ERP Ref</th>
                          <th className="p-3">Assigned Associate</th>
                          <th className="p-3">Department</th>
                          <th className="p-3">Salary Weight (Monthly INR)</th>
                          <th className="p-3 text-center">Audit Status</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y text-slate-700">
                        {employees.map((emp, idx) => (
                          <tr key={emp.id} className="hover:bg-slate-50/70 transition-colors">
                            <td className="p-3 font-mono text-slate-500 font-bold">{emp.id}</td>
                            <td className="p-3 font-semibold text-slate-950">{emp.name}</td>
                            <td className="p-3 text-slate-600">{emp.department}</td>
                            <td className="p-3 font-bold text-teal-600">
                              ₹{(120000 - idx * 10000).toLocaleString("en-IN")}
                            </td>
                            <td className="p-3 text-center">
                              <span className="bg-emerald-50 text-emerald-700 text-[10px] font-bold px-2 py-0.5 rounded-full inline-block border border-emerald-100">
                                COMPLIANT
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

            </div>

            {/* Portal Footer Visual Guide */}
            <div className="mt-8 pt-4 border-t border-slate-100 text-slate-400 text-[11px] font-sans flex flex-col sm:flex-row justify-between space-y-2 sm:space-y-0">
              <span className="flex items-center space-x-1">
                <Lock className="h-3.5 w-3.5 text-blue-500" />
                <span>Spring Security OAuth2 Session Active (SSL Secured)</span>
              </span>
              <span>Workspace Database: local H2 memory block</span>
            </div>

          </div>

        </div>

        {/* RIGHT COMPONENT: The Interactive Spring Boot IDE (Developer Code Studio) */}
        <div className="xl:col-span-5 flex flex-col space-y-4">
          
          {/* Spring Boot Code Viewer / Editor */}
          <div className="bg-[#1e293b] rounded-2xl border border-slate-700/80 shadow-2xl flex flex-col overflow-hidden h-[420px]">
            
            {/* Header Tabs */}
            <div className="bg-[#0f172a] px-4 py-2 flex justify-between items-center border-b border-slate-800">
              
              <div className="flex items-center space-x-2">
                <span className="h-3 w-3 rounded-full bg-red-500"></span>
                <span className="h-3 w-3 rounded-full bg-yellow-500"></span>
                <span className="h-3 w-3 rounded-full bg-green-500"></span>
                <span className="text-slate-500 text-xs px-2">|</span>
                <span className="text-slate-400 font-mono text-xs font-bold leading-none">Spring Boot Dev Workspace</span>
              </div>

              {/* Maven trigger block */}
              <button
                onClick={triggerMavenBuild}
                className="bg-blue-600/10 hover:bg-blue-600/20 text-blue-400 hover:text-blue-300 font-mono text-[10px] border border-blue-500/20 py-1 px-2.5 rounded cursor-pointer transition-all"
              >
                mvn clean install
              </button>

            </div>

            {/* File explorer tabs */}
            <div className="bg-[#111827] px-2 py-1.5 flex items-center space-x-1 overflow-x-auto border-b border-slate-800">
              {javaSources.map((file) => (
                <button
                  key={file.name}
                  id={`java-tab-${file.name.replace(/\./g, '-')}`}
                  onClick={() => {
                    setSelectedJavaFile(file);
                    logToConsole(`IDE: Opened database file [${file.name}]`);
                  }}
                  className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-md text-[11px] font-mono cursor-pointer transition-all ${
                    selectedJavaFile.name === file.name
                      ? "bg-[#1e293b] text-blue-400 font-bold border-t border-blue-500"
                      : "text-slate-500 hover:bg-[#1f2937]/50"
                  }`}
                >
                  <FileCode className="h-3.5 w-3.5 text-orange-400" />
                  <span>{file.name}</span>
                </button>
              ))}
            </div>

            {/* File description context bar */}
            <div className="bg-slate-900/50 px-4 py-2 text-[10px] text-slate-400 border-b border-slate-800 font-mono">
              <strong>Path:</strong> {selectedJavaFile.path} <span className="opacity-40">|</span> <em>{selectedJavaFile.description}</em>
            </div>

            {/* Editor Area */}
            <div className="flex-1 p-2 bg-[#111827] font-mono text-xs overflow-auto relative">
              <textarea
                value={modifiedCode}
                onChange={(e) => setModifiedCode(e.target.value)}
                className="w-full h-full bg-transparent text-slate-300 font-mono text-xs focus:outline-none resize-none leading-relaxed p-2"
                spellCheck="false"
              />
            </div>

          </div>

          {/* AI spring compiler counselor (Powered by Gemini) */}
          <div className="bg-slate-950 border border-slate-800 rounded-2xl p-5 shadow-2xl flex flex-col space-y-4">
            
            <div className="flex justify-between items-center">
              <div className="flex items-center space-x-2">
                <Sparkles className="h-5 w-5 text-blue-400 animate-pulse" />
                <h3 className="text-sm font-bold text-white font-display">Spring Enterprise AI Counselor</h3>
              </div>
              <span className="text-[10px] bg-slate-800 text-slate-400 border border-slate-700 py-0.5 px-2 rounded-full font-mono">
                model: gemini-3.5-flash
              </span>
            </div>

            {/* Suggested prompts array */}
            <div className="space-y-1.5">
              <span className="text-[10px] uppercase font-bold tracking-wider text-slate-500 block">Instant Spring Templates:</span>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-1.5">
                {promptChips.map((chip, idx) => (
                  <button
                    key={idx}
                    onClick={() => {
                      setAiPrompt(chip.text);
                      handleAskAI(chip.text);
                    }}
                    className="text-left text-[11px] bg-slate-900 hover:bg-slate-800/80 p-2 rounded-lg text-slate-300 border border-slate-800 hover:border-blue-500/30 transition-all truncate"
                    title={chip.text}
                  >
                    🚀 {chip.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Prompt input write bar */}
            <div className="flex space-x-2">
              <input
                type="text"
                placeholder="Ask to refactor Employee.java, add Spring validation or create security configs..."
                value={aiPrompt}
                onChange={(e) => setAiPrompt(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleAskAI()}
                className="flex-1 text-xs bg-slate-900 border border-slate-800 rounded-xl p-3 text-slate-200 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 font-mono"
              />
              <button
                onClick={() => handleAskAI()}
                disabled={aiLoading}
                className="bg-blue-600 hover:bg-blue-700 text-white p-3 rounded-xl disabled:opacity-50 transition-all flex items-center justify-center cursor-pointer"
              >
                {aiLoading ? (
                  <RefreshCw className="h-4 w-4 animate-spin" />
                ) : (
                  <Send className="h-4 w-4" />
                )}
              </button>
            </div>

            {/* Gemini output window */}
            {(aiResponse || aiLoading || aiError) && (
              <div className="border border-slate-850 bg-slate-900/40 rounded-xl p-4 space-y-3 max-h-[260px] overflow-y-auto">
                <div className="flex justify-between items-center pb-2 border-b border-slate-800/80">
                  <span className="text-xs font-bold text-slate-400">AI Counselor Proposal:</span>
                  {aiResponse && (
                    <button
                      onClick={handleInjectCodeFromAI}
                      className="text-[10px] bg-blue-600 hover:bg-blue-700 text-white font-mono px-2 py-1 rounded cursor-pointer transition-all"
                    >
                      Apply Code context to IDE
                    </button>
                  )}
                </div>

                {aiLoading && (
                  <div className="text-xs text-slate-400 font-mono flex items-center space-x-2 py-4">
                    <RefreshCw className="h-4 w-4 animate-spin text-blue-500" />
                    <span>Analyzing Java Spring parameters & generating clean, optimized bytecode templates...</span>
                  </div>
                )}

                {aiError && (
                  <div className="text-xs text-red-400 bg-red-900/10 border border-red-900/20 p-2 rounded-lg flex items-center space-x-1.5">
                    <AlertCircle className="h-4 w-4 shrink-0" />
                    <span>{aiError}</span>
                  </div>
                )}

                {aiResponse && (
                  <div className="text-xs text-slate-300 font-mono whitespace-pre-wrap leading-relaxed select-text select-all">
                    {aiResponse}
                  </div>
                )}
              </div>
            )}

          </div>

          {/* Real-time Spring Server / H2 DB terminal simulator */}
          <div className="bg-slate-950 border border-slate-850 p-4 rounded-xl shadow-lg flex-1 min-h-[140px] flex flex-col justify-between">
            <div className="flex justify-between items-center mb-1 pb-1 border-b border-slate-900">
              <span className="text-[10px] uppercase font-bold tracking-wider text-slate-500 flex items-center space-x-1">
                <span>🟢 Embedded Tomcat & JVM JPA Stream Logs (/stdout)</span>
              </span>
              <button
                onClick={() => setConsoleLogs([])}
                className="text-[9px] text-slate-500 hover:text-slate-400"
              >
                Clear Console
              </button>
            </div>

            <div className="flex-1 overflow-y-auto max-h-[110px] space-y-1 pr-1 font-mono text-[10px] text-slate-300">
              {consoleLogs.map((log, idx) => (
                <div key={idx} className="leading-relaxed hover:bg-slate-900/50 rounded p-0.5">
                  {log}
                </div>
              ))}
            </div>
          </div>

        </div>

          </div>

        </div>

      </main>

    </div>
  );
}
