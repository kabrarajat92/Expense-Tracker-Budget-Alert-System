import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BudgetService, Budget } from '../../../services/budget.service';
import { ExpenseService, Expense } from '../../../services/expense.service';

@Component({
  selector: 'app-budgets',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './budgets.component.html',
  styleUrl: './budgets.component.scss'
})
export class BudgetsComponent implements OnInit {
  budgets: Budget[] = [];
  expenses: Expense[] = [];
  newBudget: Budget = { category: '', limitAmount: 0 };
  saving = false;

  constructor(private budgetService: BudgetService, private expenseService: ExpenseService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.budgetService.getBudgets().subscribe((data) => (this.budgets = data));
    this.expenseService.getExpenses().subscribe((data) => (this.expenses = data));
  }

  saveBudget(): void {
    if (!this.newBudget.category || !this.newBudget.limitAmount) return;
    this.saving = true;
    this.budgetService.setBudget(this.newBudget).subscribe({
      next: () => {
        this.newBudget = { category: '', limitAmount: 0 };
        this.saving = false;
        this.load();
      },
      error: () => (this.saving = false)
    });
  }

  spentFor(category: string): number {
    return this.expenses
      .filter((e) => e.category === category)
      .reduce((sum, e) => sum + Number(e.amount), 0);
  }

  pctFor(category: string, limit: number): number {
    return limit > 0 ? Math.min((this.spentFor(category) / limit) * 100, 100) : 0;
  }
}