import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExpenseService, Expense } from '../../../services/expense.service';
import { BudgetService, Budget } from '../../../services/budget.service';
import { NotificationService, Notification } from '../../../services/notification.service';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss'
})
export class OverviewComponent implements OnInit {
  expenses: Expense[] = [];
  budgets: Budget[] = [];
  unread: Notification[] = [];

  constructor(
    private expenseService: ExpenseService,
    private budgetService: BudgetService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.expenseService.getExpenses().subscribe((data) => (this.expenses = data));
    this.budgetService.getBudgets().subscribe((data) => (this.budgets = data));
    this.notificationService.getUnread().subscribe((data) => (this.unread = data));
  }

  get totalSpent(): number {
    return this.expenses.reduce((sum, e) => sum + Number(e.amount), 0);
  }

  get totalBudgeted(): number {
    return this.budgets.reduce((sum, b) => sum + Number(b.limitAmount), 0);
  }

  get recentExpenses(): Expense[] {
    return [...this.expenses]
      .sort((a, b) => (b.expenseDate > a.expenseDate ? 1 : -1))
      .slice(0, 6);
  }

  budgetStatus(category: string): { spent: number; limit: number; pct: number } {
    const budget = this.budgets.find((b) => b.category === category);
    const spent = this.expenses
      .filter((e) => e.category === category)
      .reduce((sum, e) => sum + Number(e.amount), 0);
    const limit = budget?.limitAmount || 0;
    const pct = limit > 0 ? Math.min((spent / limit) * 100, 100) : 0;
    return { spent, limit, pct };
  }
}