<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\UsageStatController;

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| contains the "web" middleware group. Now create something great!
|
*/

Route::get('/', function () {
    return view('welcome');
});

Route::controller(UsageStatController::class)->group(function () {
    Route::get('/usage_stats', 'index');
    Route::post('/usage_stats', 'store');
    Route::get('usage_stats/{id}', 'show');
});

Route::get('/token', function (Request $request) {
    $token = csrf_token();
    return $token;
});
