<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateUsageStatTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('usage_stat', function (Blueprint $table) {
            $table->id();
            //Titan Eclipse Usage Stat data Schema
            $table->string("plugin_id");
            $table->string("plugin_version_qualifier");
            $table->string("plugin_version");
            $table->string("os_version");
            $table->string("os_arch");
            $table->string("eclipse_version");
            $table->string("eclipse_version_qualifier");
            $table->string("user_id");
            $table->string("java_version");
            $table->string("os_name");
            $table->string("info");

            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::dropIfExists('usage_stat');
    }
}
