import { Component } from '@angular/core';
import { NavBar } from '../nav-bar/nav-bar';
import { Footer } from '../footer/footer';

@Component({
  selector: 'app-home-page',
  imports: [NavBar, Footer],
  templateUrl: './home-page.html',
  styleUrls: ['./home-page.css']
})
export class HomePage {

}
