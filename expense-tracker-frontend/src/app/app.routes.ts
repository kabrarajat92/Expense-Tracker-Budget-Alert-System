import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { DashboardLayoutComponent } from './pages/dashboard/dashboard-layout/dashboard-layout.component';
import { OverviewComponent } from './pages/dashboard/overview/overview.component';
import { ExpensesComponent } from './pages/dashboard/expenses/expenses.component';
import { BudgetsComponent } from './pages/dashboard/budgets/budgets.component';
import { AlertsComponent } from './pages/dashboard/alerts/alerts.component';
import { AssistantComponent } from './pages/dashboard/assistant/assistant.component';
import { authGuard } from './guards/auth.guard';
import { RegisterComponent } from './pages/register/register.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'dashboard',
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'overview', pathMatch: 'full' },
      { path: 'overview', component: OverviewComponent },
      { path: 'expenses', component: ExpensesComponent },
      { path: 'budgets', component: BudgetsComponent },
      { path: 'alerts', component: AlertsComponent },
      { path: 'assistant', component: AssistantComponent }
    ]
  }
];