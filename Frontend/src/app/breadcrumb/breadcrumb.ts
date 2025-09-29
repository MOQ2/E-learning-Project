import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router, NavigationEnd, RouterLink} from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.html',
  imports: [
    RouterLink
  ],
  styleUrls: ['breadcrumb.css']
})
export class BreadcrumbComponent implements OnInit {
  breadcrumbs: Breadcrumb[] = [];

  constructor(private router: Router, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.breadcrumbs = this.buildBreadcrumbs(this.route.root);
      });
  }

  private buildBreadcrumbs(route: ActivatedRoute, url: string = '', breadcrumbs: Breadcrumb[] = []): Breadcrumb[] {
    const routeURL = route.snapshot.url.map(segment => segment.path).join('/');
    if (routeURL) url += `/${routeURL}`;

    let label = route.snapshot.data['breadcrumb'];

    if (route.snapshot.paramMap.has('id') && label === 'QuizSubmission') {
      label = 'Submission';
    }

    if (label) {
      if (!breadcrumbs.find(b => b.label === label)) {
        breadcrumbs.push({ label, url });
      } else {
        const existing = breadcrumbs.find(b => b.label === label);
        if (existing) existing.url = url;
      }
    }
    if (route.firstChild) {
      this.buildBreadcrumbs(route.firstChild, url, breadcrumbs);
    }

    return breadcrumbs;
  }
}
