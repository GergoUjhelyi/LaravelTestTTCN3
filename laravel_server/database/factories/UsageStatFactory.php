<?php

namespace Database\Factories;

use App\Models\UsageStat;
use Illuminate\Database\Eloquent\Factories\Factory;

class UsageStatFactory extends Factory
{
    /**
     * Define the model's default state.
     *
     * @return array
     */
    protected $model = UsageStat::class;


    public function definition()
    {
        $os_names = ['windows', 'linux'];
        return [
            'plugin_id' => 'Eclipse Titan Java',
            'plugin_version_qualifier' => 'Thesis Edition',
            'plugin_version' => $this->faker->semver(),
            'os_version' => 'Example Version',
            'os_arch' => 'x86-64',
            'eclipse_version' => $this->faker->semver(false, true),
            'eclipse_version_qualifier' => '',
            'user_id' => $this->faker->bothify('user-??????####'),
            'java_version' => $this->faker->semver(),
            'os_name' => $this->faker->randomElement($os_names),
            'info' => $this->faker->word()
        ];
    }
}
