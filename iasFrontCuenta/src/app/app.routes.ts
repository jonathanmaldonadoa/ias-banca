import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'cuentas', pathMatch: 'full' },
  {
    path: 'cuentas',
    loadChildren: () =>
      import('./features/cuentas/cuentas.routes').then((module) => module.cuentasRoutes),
  },
  {
    path: 'transferencias',
    loadChildren: () =>
      import('./features/transferencias/transferencias.routes').then(
        (module) => module.transferenciasRoutes,
      ),
  },
  { path: '**', redirectTo: 'cuentas' },
];
