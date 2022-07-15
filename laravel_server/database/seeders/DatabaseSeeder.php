<?php

namespace Database\Seeders;

use App\Models\UsageStat;
use Faker\Factory;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     *
     * @return void
     */
    public function run()
    {
        $faker = Factory::create();
        DB::table('usage_stats')->truncate();

        UsageStat::factory($faker->numberBetween(10,30))->create();
    }
}
