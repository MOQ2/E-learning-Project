import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {MatDialogModule} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet , MatDialogModule, MatButtonModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {


}
