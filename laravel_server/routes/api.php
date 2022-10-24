<?php

use App\Http\Controllers\UsageStatController;
use App\Models\UsageStat;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| is assigned the "api" middleware group. Enjoy building your API!
|
*/

Route::middleware('auth:sanctum')->get('/user', function (Request $request) {
    return $request->user();
});

Route::controller(UsageStatController::class)->group(function () {
    Route::get('/usage_stats', 'index');
    Route::post('/usage_stats', 'store');
    Route::get('/usage_stats/{id}', 'show');
    Route::delete('/usage_stats/{id}', 'destroy');
});

Route::get('/token', function (Request $request) {
    $token = csrf_token();
    return $token;
});

Route::get('/last_id', function (Request $request) {
    $usage_stat = UsageStat::latest()->first();
    return $usage_stat->id;
});
