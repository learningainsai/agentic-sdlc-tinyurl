import { Component } from '@angular/core';

import { ShortenComponent } from './feature/shorten/shorten.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ShortenComponent],
  template: `<app-shorten></app-shorten>`,
})
export class AppComponent {}
