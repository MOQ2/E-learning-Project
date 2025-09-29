import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {NavBar} from './components/nav-bar/nav-bar';
import {Footer} from './components/footer/footer';
import {BreadcrumbComponent} from './breadcrumb/breadcrumb';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, MatDialogModule, MatButtonModule, NavBar, Footer, BreadcrumbComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {


}
