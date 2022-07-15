<?php

namespace Database\Seeders;

use App\Models\UsageStat;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class UsageStatSeeder extends Seeder
{
    /**
     * Run the database seeds.
     *
     * @return void
     */
    public function run()
    {
        DB::table('usage_stats')->truncate();
        UsageStat::factory(30)->create();
    }
}
