# Add lib to load path
$LOAD_PATH.unshift File.join(File.dirname(__FILE__), '..', 'lib')

require 'rsoar'

import org.jsoar.kernel.SoarProperties

agent = RSoar::RAgent.new do |a|
  a.name = "Hello"

  #a.properties[SoarProperties::WAITSNC] = true
  a.eval "waitsnc --on"

  a.sp 'hello
    (state <s> ^superstate nil ^io.input-link.cycle-count <cc>)
  -->
    (write (crlf) |Hello, RubySoar | <cc>)
    (wait)
  '

  a.sp 'monitor-input
    (state <s> ^superstate nil ^io.input-link.<name> <value>)
  -->
    (write (crlf) |Input: | <name> |=| <value>)
  '

  a.sp 'hello2
    (state <s> ^superstate nil ^io.input-link.cycle-count 100)
  -->
    (write (crlf) |Hello, RubySoar |)
    (interrupt)
    (debug)
  '
end

root = agent.input.root

agent.run_forever

root.+ :foo, 99
um = root.+ :um do |um|
  um.students = 40000
  um.colors = "Maize and Blue"
end

java.lang.Thread.sleep 2000

puts "Waiting = #{agent.properties[SoarProperties::WAIT_INFO]}"
agent.input.atomic do |input|
  root.^ :foo, 100
  root.+ :location do |l|
    l.x=100
    l.y=3.14
    l.name="Ann Arbor"
    l.+ :info do |i|
      i.population = 100000
      i.area = "100 mi^sq"
      i.contains = um
    end
  end
end

java.lang.Thread.sleep 2000
root.- :foo
agent.open_debugger
